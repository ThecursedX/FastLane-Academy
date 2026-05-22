/**
 * js/nav.js
 * Shared nav auth state + theme toggle for public pages (index, courses, feedback).
 * Must load AFTER api.js.
 */

/* ─── Theme ─────────────────────────────────────────────────────────────── */

function applyNavTheme(theme) {
    document.documentElement.dataset.theme = theme;
    const moon = document.getElementById("navMoon");
    const sun  = document.getElementById("navSun");
    if (moon) moon.style.display = theme === "dark" ? "none"  : "block";
    if (sun)  sun.style.display  = theme === "dark" ? "block" : "none";
}

window.toggleThemeNav = function () {
    const next = document.documentElement.dataset.theme === "dark" ? "light" : "dark";
    localStorage.setItem("fl-theme", next);
    applyNavTheme(next);
};

// Apply saved theme immediately (before paint)
applyNavTheme(localStorage.getItem("fl-theme") || "light");

document.addEventListener("keydown", e => {
    if (e.altKey && e.key === "d") toggleThemeNav();
});

/* ─── Auth state ─────────────────────────────────────────────────────────── */

window.navLogout = async function () {
    try { await fetch("/api/auth/logout", { method: "POST", credentials: "include" }); } catch (_) {}
    localStorage.removeItem("fl-userId");
    localStorage.removeItem("fl-role");
    localStorage.removeItem("fl-email");
    window.location.href = "index.html";
};

document.addEventListener("DOMContentLoaded", function () {
    const role   = localStorage.getItem("fl-role");
    const userId = localStorage.getItem("fl-userId");

    const signIn    = document.getElementById("nav-signin");
    const book      = document.getElementById("nav-book");
    const dashboard = document.getElementById("nav-dashboard");
    const logout    = document.getElementById("nav-logout");

    if (role && userId) {
        const href = role.toUpperCase() === "INSTRUCTOR" ? "instructor.html"
            : role.toUpperCase() === "ADMIN"      ? "admin.html"
                : "student.html";
        if (dashboard) { dashboard.href = href; dashboard.style.display = ""; }
        if (logout)    { logout.style.display = ""; }
        if (signIn)    { signIn.style.display = "none"; }
        if (book)      { book.style.display = "none"; }
    } else {
        if (signIn) { signIn.style.display = ""; }
        if (book)   { book.style.display = ""; }
        if (dashboard) { dashboard.style.display = "none"; }
        if (logout)    { logout.style.display = "none"; }
    }
});
