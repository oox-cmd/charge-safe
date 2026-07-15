package com.chargesafe.security;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionCodeConsistencyTest {

    private static final Pattern SQL_PERMISSION = Pattern.compile(
            "'([a-z][a-z0-9-]*(?::[a-z][a-z0-9-]*)+)'"
    );
    private static final Pattern ADMIN_PERMISSION = Pattern.compile(
            "(?:v-permission=\\\"'([^']+)'\\\"|hasPermission\\('([^']+)'\\))"
    );

    @Test
    void controllerPermissionCodesExistInFlywayMenuData() throws IOException {
        Set<String> backendCodes = backendPermissionCodes();
        Set<String> menuCodes = migrationPermissionCodes();

        Set<String> missing = new HashSet<>(backendCodes);
        missing.removeAll(menuCodes);

        assertTrue(missing.isEmpty(), "Controller permissions missing from Flyway menu data: " + missing);
    }

    @Test
    void adminPermissionChecksUseControllerAndFlywayPermissionCodes() throws IOException {
        Set<String> backendCodes = backendPermissionCodes();
        Set<String> menuCodes = migrationPermissionCodes();
        Set<String> adminCodes = adminPermissionCodes();

        Set<String> missingFromBackend = new HashSet<>(adminCodes);
        missingFromBackend.removeAll(backendCodes);
        Set<String> missingFromMenu = new HashSet<>(adminCodes);
        missingFromMenu.removeAll(menuCodes);

        assertTrue(missingFromBackend.isEmpty(),
                "Admin permissions missing from Controller annotations: " + missingFromBackend);
        assertTrue(missingFromMenu.isEmpty(),
                "Admin permissions missing from Flyway menu data: " + missingFromMenu);
    }

    @Test
    void adminPermissionHelperDoesNotDefineASecondRolePermissionMatrix() throws IOException {
        String helper = Files.readString(Path.of("../charge-safe-admin/src/composables/usePermission.js"));

        assertTrue(!helper.contains("fallbackRolePermissionMatrix"),
                "Admin permission checks must use permissions returned by the backend");
        assertTrue(!helper.contains("granted.endsWith(':*')"),
                "Admin permission checks must not invent wildcard permission semantics");
    }

    private Set<String> backendPermissionCodes() {
        Set<String> codes = new HashSet<>();
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        scanner.findCandidateComponents("com.chargesafe.modules").forEach(component -> {
            try {
                Class<?> controller = Class.forName(component.getBeanClassName());
                for (Method method : controller.getDeclaredMethods()) {
                    RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
                    if (annotation != null) {
                        codes.add(annotation.value());
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load Controller " + component.getBeanClassName(), e);
            }
        });
        return codes;
    }

    private Set<String> migrationPermissionCodes() throws IOException {
        Path migrationDirectory = Path.of("src/main/resources/db/migration");
        Set<String> codes = new HashSet<>();
        try (Stream<Path> paths = Files.list(migrationDirectory)) {
            for (Path path : paths
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().matches("V[0-9]+__.*\\.sql"))
                    .toList()) {
                codes.addAll(matches(Files.readString(path), SQL_PERMISSION));
            }
        }
        return codes;
    }

    private Set<String> adminPermissionCodes() throws IOException {
        Path adminSource = Path.of("../charge-safe-admin/src");
        Set<String> codes = new HashSet<>();
        try (Stream<Path> paths = Files.walk(adminSource)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                if (path.toString().endsWith(".vue") || path.toString().endsWith(".js")) {
                    codes.addAll(matches(Files.readString(path), ADMIN_PERMISSION));
                }
            }
        }
        return codes;
    }

    private Set<String> matches(String content, Pattern pattern) {
        Set<String> values = new HashSet<>();
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            values.add(value);
        }
        return values;
    }
}
