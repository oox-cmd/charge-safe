package com.chargesafe.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceDataMigrationContractTest {

    private static final Path V7 = Path.of(
            "src/main/resources/db/migration/V7__add_dept_and_device_category.sql");

    @Test
    void v7CreatesReferenceDataTablesAndBusinessAssociations() throws IOException {
        assertThat(V7).exists();
        String sql = Files.readString(V7);

        assertThat(sql)
                .contains("CREATE TABLE IF NOT EXISTS `sys_dept`")
                .contains("CREATE TABLE IF NOT EXISTS `device_category`")
                .contains("ALTER TABLE `sys_user`")
                .contains("ADD COLUMN `dept_id`")
                .contains("ALTER TABLE `charging_device`")
                .contains("ADD COLUMN `category_id`")
                .contains("uk_dept_code")
                .contains("uk_category_code");
    }

    @Test
    void v7GrantsReferenceDataMutationsOnlyToPlatformAndResidentRepairsOnlyToResidents() throws IOException {
        String sql = Files.readString(V7);

        assertThat(sql)
                .contains("SELECT 1, `id` FROM `sys_menu` WHERE `id` IN (1140, 1141, 1142, 1143, 1330, 1331, 1332, 1333, 1340, 1730)")
                .contains("SELECT 2, `id` FROM `sys_menu` WHERE `id` IN (1330, 1730)")
                .contains("resident:repair:create")
                .contains("resident:repair:view")
                .contains("role_record.`role_code` = 'RESIDENT'")
                .doesNotContain("SELECT 2, `id` FROM `sys_menu` WHERE `id` IN (1141");
    }
}
