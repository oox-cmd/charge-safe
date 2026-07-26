package com.chargesafe.security;

import com.chargesafe.common.PageQuery;
import com.chargesafe.modules.log.controller.LogController;
import com.chargesafe.modules.user.controller.MenuController;
import com.chargesafe.modules.user.controller.RoleController;
import com.chargesafe.modules.user.dto.RoleQueryDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagementQueryPermissionContractTest {

    @Test
    void managementReadEndpointsRequireTheirMenuViewPermission() throws NoSuchMethodException {
        assertPermission(RoleController.class, "pageList", "system:role:view", RoleQueryDTO.class, PageQuery.class);
        assertPermission(RoleController.class, "listEnabled", "system:role:view");
        assertPermission(MenuController.class, "listTree", "system:menu:view", Boolean.class);
        assertPermission(MenuController.class, "tree", "system:menu:view", Boolean.class);
        assertPermission(LogController.class, "pageList", "operation-log:view",
                String.class, String.class, String.class, PageQuery.class);
        assertPermission(LogController.class, "getById", "operation-log:view", Long.class);
    }

    private void assertPermission(Class<?> type, String methodName, String expected, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = type.getDeclaredMethod(methodName, parameterTypes);
        RequiresPermission permission = method.getAnnotation(RequiresPermission.class);
        assertEquals(expected, permission == null ? null : permission.value(),
                () -> type.getSimpleName() + "." + methodName + " must declare " + expected);
    }
}
