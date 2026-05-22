package com.example.FastLane.Academy.util;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    public static boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("role") != null;
    }

    public static boolean isRole(HttpSession session, String role) {
        Object sessionRole = session.getAttribute("role");
        return sessionRole != null && sessionRole.toString().equals(role);
    }

    /**
     * Returns the userId stored in the session at login time.
     * AuthService must call session.setAttribute("userId", ...) on login.
     */
    public static String getUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId != null ? userId.toString() : null;
    }
}