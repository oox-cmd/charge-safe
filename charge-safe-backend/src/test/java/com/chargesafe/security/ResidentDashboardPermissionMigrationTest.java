package com.chargesafe.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ResidentDashboardPermissionMigrationTest {

    private static final Path V6 = Path.of(
            "src/main/resources/db/migration/V6__add_resident_dashboard_permission.sql");

    @Test
    void v6CreatesHiddenResidentPermissionAndIdempotentRoleBindingUsingStableCodes() throws IOException {
        assertThat(V6).exists();
        String sql = Files.readString(V6);

        assertThat(sql)
                .contains("resident:dashboard:view")
                .contains("role_record.`role_code` = 'RESIDENT'")
                .contains("permission_record.`permission_code` = 'resident:dashboard:view'")
                .contains("NOT EXISTS")
                .doesNotContain("statistics:bigscreen:view");
        assertThat(sql.split("INSERT INTO `sys_menu`", -1)).hasSize(2);
        assertThat(sql.split("INSERT INTO `sys_role_menu`", -1)).hasSize(2);
    }

    @Test
    void v6DoesNotGrantDashboardPermissionsToAnyOtherRoleOrModifySchema() throws IOException {
        assertThat(V6).exists();
        String sql = Files.readString(V6);

        assertThat(sql).doesNotContain("PLATFORM_ADMIN", "PROPERTY_ADMIN", "MAINTAINER", "INSPECTOR");
        assertThat(sql.toUpperCase())
                .doesNotContain("ALTER TABLE")
                .doesNotContain("DROP TABLE")
                .doesNotContain("DELETE FROM")
                .doesNotContain("UPDATE ");
    }
}
