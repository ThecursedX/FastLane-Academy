let currentMode = "signin";
let currentRole = "student";

document.addEventListener("DOMContentLoaded", () => {
    setupTheme();
    updateFormState();

    const form = document.querySelector("form");
    if (form) {
        form.addEventListener("submit", handleAuthSubmit);
    }

    document.addEventListener("keydown", e => {
        if (e.altKey && e.key === "d") {
            document.getElementById("themeToggle")?.click();
        }
    });
});

function setupTheme() {
    const html = document.documentElement;
    const stored = localStorage.getItem("fl-theme") || "light";
    applyTheme(stored);

    document.getElementById("themeToggle")?.addEventListener("click", () => {
        const next = html.dataset.theme === "dark" ? "light" : "dark";
        applyTheme(next);
        localStorage.setItem("fl-theme", next);
    });
}

function applyTheme(theme) {
    document.documentElement.dataset.theme = theme;

    const label = document.getElementById("themeLabel");
    if (label) {
        label.textContent = theme === "dark" ? "Light" : "Dark";
    }
}

function toggleMode() {
    currentMode = currentMode === "signin" ? "signup" : "signin";
    updateFormState();
}

function setRole(role) {
    currentRole = role;

    document.getElementById("tabStudent")?.classList.toggle("active", role === "student");
    document.getElementById("tabInstructor")?.classList.toggle("active", role === "instructor");

    document.getElementById("tabStudent")?.setAttribute("aria-selected", role === "student");
    document.getElementById("tabInstructor")?.setAttribute("aria-selected", role === "instructor");

    updateFormState();
}

function updateFormState() {
    const isSignUp = currentMode === "signup";
    const isInstructor = currentRole === "instructor";

    const modeLabel = document.getElementById("modeLabel");
    const authTitle = document.getElementById("authTitle");
    const switchPrompt = document.getElementById("switchPrompt");
    const modeToggle = document.getElementById("modeToggle");
    const submitLabel = document.getElementById("submitLabel");

    if (modeLabel) {
        modeLabel.textContent = isSignUp
            ? isInstructor ? "Join as instructor" : "Create your account"
            : "Welcome back";
    }

    if (authTitle) {
        authTitle.innerHTML = isSignUp
            ? isInstructor ? "Apply as an<br>instructor" : "Start your<br>journey"
            : "Sign in to<br>your account";
    }

    if (switchPrompt) {
        switchPrompt.textContent = isSignUp
            ? "Already have an account?"
            : "Don't have an account?";
    }

    if (modeToggle) {
        modeToggle.textContent = isSignUp ? " Sign in" : " Sign up";
    }

    toggle("nameRow", isSignUp);
    toggle("confirmField", isSignUp);
    toggle("termsNote", isSignUp);
    toggle("licenseField", isSignUp && isInstructor);
    toggle("forgotLink", !isSignUp);

    if (submitLabel) {
        submitLabel.textContent = isSignUp
            ? isInstructor ? "Apply to instruct" : "Create account"
            : "Sign in";
    }

    const inner = document.querySelector(".auth-form-inner");
    if (inner) {
        inner.classList.remove("mode-switch-anim");
        void inner.offsetWidth;
        inner.classList.add("mode-switch-anim");
    }
}

function toggle(id, show) {
    document.getElementById(id)?.classList.toggle("hidden", !show);
}

async function handleAuthSubmit(e) {
    e.preventDefault();

    const btn = document.getElementById("submitBtn");
    const label = document.getElementById("submitLabel");

    const email = document.getElementById("email")?.value.trim();
    const password = document.getElementById("password")?.value;
    const firstName = document.getElementById("firstName")?.value.trim();
    const lastName = document.getElementById("lastName")?.value.trim();
    const confirmPassword = document.getElementById("confirmPassword")?.value;
    const licenseId = document.getElementById("license")?.value.trim();

    try {
        if (btn) btn.disabled = true;
        if (label) label.textContent = "Please wait…";

        if (currentMode === "signup") {
            if (password !== confirmPassword) {
                alert("Passwords do not match");
                return;
            }

            const endpoint = currentRole === "instructor"
                ? "/auth/register/instructor"
                : "/auth/register/student";

            const body = currentRole === "instructor"
                ? { firstName, lastName, email, password, licenseId }
                : { firstName, lastName, email, password };

            await apiJson(endpoint, {
                method: "POST",
                body: JSON.stringify(body)
            });

            alert("Account created successfully. Please sign in.");
            currentMode = "signin";
            updateFormState();
            return;
        }

        const loginData = await apiJson("/auth/login", {
            method: "POST",
            body: JSON.stringify({
                email,
                password,
                role: currentRole.toUpperCase()
            })
        });

        console.log("Login response:", loginData);

        alert("Login successful");

        if (currentRole === "instructor") {
            window.location.href = "instructor.html";
        } else {
            window.location.href = "student.html";
        }


    } catch (error) {
        alert(error.message);
    } finally {
        if (btn) btn.disabled = false;
        updateFormState();
    }
}

function handleOAuth() {
    alert("Google login is not connected yet.");
}