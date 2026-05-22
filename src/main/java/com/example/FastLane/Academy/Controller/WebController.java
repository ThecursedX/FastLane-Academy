package com.example.FastLane.Academy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Maps clean URLs to HTML files served from resources/static/.
 * This removes the .html extension from the browser address bar.
 *
 * Examples:
 *   /              → index.html   (already handled by Spring welcome page)
 *   /login         → auth.html
 *   /courses       → courses.html
 *   /student       → student.html
 *   /instructor    → instructor.html
 *   /admin         → admin.html
 *   /feedback      → feedback.html
 */
@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "forward:/auth.html";
    }

    @GetMapping("/courses")
    public String courses() {
        return "forward:/courses.html";
    }

    @GetMapping("/student")
    public String student() {
        return "forward:/student.html";
    }

    @GetMapping("/instructor")
    public String instructor() {
        return "forward:/instructor.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "forward:/feedback.html";
    }
}
