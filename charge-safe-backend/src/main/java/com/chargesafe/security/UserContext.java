package com.chargesafe.security;

/**
 * 用户上下文 - 使用 ThreadLocal 存储当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(LoginUser user) {
        USER_HOLDER.set(user);
    }

    public static LoginUser getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = getUser();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        LoginUser user = getUser();
        return user != null ? user.getUsername() : null;
    }

    public static String getRoleCode() {
        LoginUser user = getUser();
        return user != null ? user.getRoleCode() : null;
    }

    public static Long getCommunityId() {
        LoginUser user = getUser();
        return user != null ? user.getCommunityId() : null;
    }

    public static void remove() {
        USER_HOLDER.remove();
    }
}
