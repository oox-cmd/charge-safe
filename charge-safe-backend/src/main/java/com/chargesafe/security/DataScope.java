package com.chargesafe.security;

/**
 * 数据权限（行级）辅助工具
 *
 * <p>规则：
 * <ul>
 *     <li>PLATFORM_ADMIN：平台管理员，可见全部数据</li>
 *     <li>PROPERTY_ADMIN：物业管理员，仅可见所属社区数据</li>
 *     <li>MAINTAINER：运维人员，仅可见分配给自己的工单，其余按所属社区过滤</li>
 *     <li>INSPECTOR：巡检人员，仅可见分配给自己的任务，其余按所属社区过滤</li>
 *     <li>RESIDENT：居民用户，仅可见自己创建/拥有的数据</li>
 * </ul>
 */
public final class DataScope {

    public static final String ROLE_PLATFORM_ADMIN = "PLATFORM_ADMIN";
    public static final String ROLE_PROPERTY_ADMIN = "PROPERTY_ADMIN";
    public static final String ROLE_MAINTAINER = "MAINTAINER";
    public static final String ROLE_INSPECTOR = "INSPECTOR";
    public static final String ROLE_RESIDENT = "RESIDENT";

    private DataScope() {
    }

    /**
     * 是否平台管理员（可见全部数据）
     */
    public static boolean isPlatformAdmin() {
        return ROLE_PLATFORM_ADMIN.equals(UserContext.getRoleCode());
    }

    /**
     * 是否物业管理员（仅本社区）
     */
    public static boolean isPropertyAdmin() {
        return ROLE_PROPERTY_ADMIN.equals(UserContext.getRoleCode());
    }

    /**
     * 是否运维人员
     */
    public static boolean isMaintainer() {
        return ROLE_MAINTAINER.equals(UserContext.getRoleCode());
    }

    /**
     * 是否巡检人员
     */
    public static boolean isInspector() {
        return ROLE_INSPECTOR.equals(UserContext.getRoleCode());
    }

    /**
     * 是否居民用户
     */
    public static boolean isResident() {
        return ROLE_RESIDENT.equals(UserContext.getRoleCode());
    }

    /**
     * 当前登录用户所属社区ID（平台管理员为 null，表示不限）
     */
    public static Long currentCommunityId() {
        return UserContext.getCommunityId();
    }

    /**
     * 当前登录用户ID
     */
    public static Long currentUserId() {
        return UserContext.getUserId();
    }
}
