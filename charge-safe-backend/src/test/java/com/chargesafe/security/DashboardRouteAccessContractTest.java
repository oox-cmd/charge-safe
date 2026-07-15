package com.chargesafe.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardRouteAccessContractTest {

    private static final Path ADMIN_SOURCE = Path.of("../charge-safe-admin/src");

    @Test
    void adminAndResidentDashboardsUseBackendPermissionsAndAuthenticatedRoutes() throws IOException {
        String router = Files.readString(ADMIN_SOURCE.resolve("router/index.js"));

        assertThat(router)
                .contains("path: '/bigscreen'")
                .contains("permission: 'statistics:bigscreen:view'")
                .contains("path: '/resident/dashboard'")
                .contains("permission: 'resident:dashboard:view'")
                .contains("roles: ['RESIDENT']")
                .contains("if (roleCode === 'RESIDENT') return '/resident/dashboard'")
                .doesNotContain("if (roleCode === 'RESIDENT') return '/bigscreen'");

        int permissionsLoaded = router.indexOf("!userStore.permissionsLoaded");
        int permissionCheck = router.indexOf("requiredPermissions");
        assertThat(permissionsLoaded).isGreaterThanOrEqualTo(0);
        assertThat(permissionCheck).isGreaterThan(permissionsLoaded);
        assertThat(router)
                .contains("record.meta.roles")
                .contains("next({ path: '/403', replace: true, query: { from: to.fullPath } })");
    }

    @Test
    void eachDashboardCallsOnlyItsDedicatedDataApi() throws IOException {
        String managementPage = Files.readString(ADMIN_SOURCE.resolve("views/bigscreen/Index.vue"));
        Path residentPage = ADMIN_SOURCE.resolve("views/resident/Dashboard.vue");

        assertThat(managementPage)
                .contains("getManagementBigscreen")
                .doesNotContain("getDashboard, getPortStatus, getOrderTrend")
                .doesNotContain("getParkingAreaUsage\n");
        assertThat(residentPage).exists();
        assertThat(Files.readString(residentPage))
                .contains("getResidentDashboard")
                .doesNotContain("@/api/statistics");
    }
}
