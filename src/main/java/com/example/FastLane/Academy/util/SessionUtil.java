package com.example.FastLane.Academy.util;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    public static boolean isLoggedIn(HttpSession session) {

        return session.getAttribute("role") != null;
    }

    public static boolean isRole(HttpSession session, String role) {

        Object sessionRole = session.getAttribute("role");

        return sessionRole != null &&
                sessionRole.toString().equals(role);
    }
}