package com.example.myeongranghoe.config;

public final class UserContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private UserContext() {}

    public static void set(String email) {
        CURRENT.set(email);
    }

    public static String get() {
        return CURRENT.get();
    }

    public static String require() {
        String email = CURRENT.get();
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("로그인이 필요해요.");
        }
        return email;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
