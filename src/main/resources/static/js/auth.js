/**
 * src/main/resources/static/js/auth.js
 *
 * Depends on: js/api.js (must load first)
 *
 * Routes wired:
 *   POST /api/auth/register/student      → AuthService.registerStudent()
 *   POST /api/auth/register/instructor   → AuthService.registerInstructor()
 *   POST /api/auth/login                 → AuthService.login()
 *   POST /api/auth/logout                → AuthService.logout()
 *
 * Login behaviour (from AuthService):
 *   - No role sent in request body — server tries Student → Instructor → Admin by email
 *   - Response content is LoginResponseDTO { userId, email, role }
 *   - Redirect is driven entirely by the role the server returns
 *
 * Admin login:
 *   - "Sign in as admin" link calls switchToAdmin() which hides the role
 *     switcher and adjusts labels — the form still POSTs to /auth/login
 *     with just email + password, same as all other roles
 *
 * RegisterDTO fields used:
 *   Student:    fullName, email, password, contactNumber, nic,
 *               address, dateOfBirth, emergencyContact, role
 *   Instructor: fullName, email, password, contactNumber, licenseId,
 *               experienceYears, vehicleType, workingDays, role
 *
 * Age validation (≥ 18) is enforced server-side for students.
 * Duplicate email / NIC / licenseId are also caught server-side.
 */

let currentMode = "signin";   // "signin" | "signup"
let currentRole = "student";  // "student" | "instructor" | "admin"

/* ─── Boot ───────────────────────────────────────────────────────────────── */

document.addEventListener("DOMContentLoaded", () => {
  setupTheme();
  updateFormState();

  document.getElementById("authForm")
      ?.addEventListener("submit", handleAuthSubmit);

  /* Alt + D → toggle dark mode */
  document.addEventListener("keydown", e => {
    if (e.altKey && e.key === "d") document.getElementById("themeToggle")?.click();
  });
});

/* ─── Theme ──────────────────────────────────────────────────────────────── */

function setupTheme() {
  const stored = localStorage.getItem("fl-theme") || "light";
  applyTheme(stored);

  document.getElementById("themeToggle")?.addEventListener("click", () => {
    const next = document.documentElement.dataset.theme === "dark" ? "light" : "dark";
    applyTheme(next);
    localStorage.setItem("fl-theme", next);
  });
}

function applyTheme(theme) {
  document.documentElement.dataset.theme = theme;
  const label = document.getElementById("themeLabel");
  if (label) label.textContent = theme === "dark" ? "Light" : "Dark";
}

/* ─── Mode / role switching ──────────────────────────────────────────────── */

function toggleMode() {
  currentMode = currentMode === "signin" ? "signup" : "signin";
  /* Admin can't register — drop back to student when entering sign-up */
  if (currentMode === "signup" && currentRole === "admin") currentRole = "student";
  clearAlert();
  updateFormState();
}

function setRole(role) {
  currentRole = role;
  document.getElementById("tabStudent")
      ?.classList.toggle("active", role === "student");
  document.getElementById("tabInstructor")
      ?.classList.toggle("active", role === "instructor");
  document.getElementById("tabStudent")
      ?.setAttribute("aria-selected", String(role === "student"));
  document.getElementById("tabInstructor")
      ?.setAttribute("aria-selected", String(role === "instructor"));
  clearAlert();
  updateFormState();
}

/**
 * Called by the "Sign in as admin" anchor.
 * Hides the role switcher + sign-up link, updates labels,
 * and leaves the form pointing at /auth/login (no change needed —
 * AuthService finds admins by email regardless of role param).
 */
function switchToAdmin() {
  currentRole = "admin";
  currentMode = "signin";
  clearAlert();
  updateFormState();
}
function switchToNormal() {
  currentRole = "student";
  currentMode = "signin";
  clearAlert();
  updateFormState();
}

function updateFormState() {
  const isSignUp     = currentMode === "signup";
  const isInstructor = currentRole === "instructor";
  const isAdmin      = currentRole === "admin";

  /* ── Labels ── */
  setText("modeLabel",
      isAdmin    ? "Admin access"
          : isSignUp ? (isInstructor ? "Join as instructor" : "Create your account")
              : "Welcome back"
  );

  setHTML("authTitle",
      isAdmin    ? "Sign in to<br>admin console"
          : isSignUp ? (isInstructor ? "Apply as an<br>instructor" : "Start your<br>journey")
              : "Sign in to<br>your account"
  );

  setText("switchPrompt",
      isSignUp ? "Already have an account?" : "Don't have an account?"
  );

  const modeToggle = document.getElementById("modeToggle");
  if (modeToggle) {
    /* Hide "Sign up" link when in admin mode — admins can't self-register */
    modeToggle.style.display = isAdmin ? "none" : "";
    modeToggle.textContent   = isSignUp ? " Sign in" : " Sign up";
  }

  setText("submitLabel",
      isAdmin    ? "Sign in as admin"
          : isSignUp ? (isInstructor ? "Apply to instruct" : "Create account")
              : "Sign in"
  );

  /* ── Role switcher visibility ── */
  /* Only show the role tabs when signing up (irrelevant during sign-in:
     the server identifies the account by email regardless of role tab) */
  const roleSwitcher = document.querySelector(".role-switcher");
  if (roleSwitcher) roleSwitcher.style.display = (isAdmin || !isSignUp) ? "none" : "";

  /* ── Field visibility ──
     Fields are shown/hidden based on mode + role.
     AuthService.registerStudent() needs:
       fullName, email, password, contactNumber, nic,
       address, dateOfBirth, emergencyContact
     AuthService.registerInstructor() needs:
       fullName, email, password, contactNumber, licenseId,
       experienceYears, vehicleType, workingDays
  */
  toggle("nameRow",               isSignUp && !isAdmin);
  toggle("confirmField",          isSignUp && !isAdmin);
  toggle("termsNote",             isSignUp && !isAdmin);
  toggle("forgotLink",            !isSignUp);
  toggle("studentExtraFields",    isSignUp && !isInstructor && !isAdmin);
  toggle("licenseField",          isSignUp && isInstructor);
  toggle("instructorExtraFields", isSignUp && isInstructor);

  /* ── Animate ── */
  const inner = document.querySelector(".auth-form-inner");
  if (inner) {
    inner.classList.remove("mode-switch-anim");
    void inner.offsetWidth;
    inner.classList.add("mode-switch-anim");
  }

  const adminLink = document.querySelector(".admin-link");

  if(adminLink){
    adminLink.innerHTML = isAdmin
        ? "← Back to normal sign in"
        : "Sign in as admin";
  }
}

/* ─── Form submit ────────────────────────────────────────────────────────── */

async function handleAuthSubmit(e) {
  e.preventDefault();
  clearAlert();

  const btn   = document.getElementById("submitBtn");
  const label = document.getElementById("submitLabel");

  /* ── Collect values ── */
  const email           = v("email");
  const password        = v("password");
  const firstName       = v("firstName");
  const lastName        = v("lastName");
  const confirmPassword = v("confirmPassword");

  /* Student fields */
  const nic              = v("nic");
  const contactNumber    = v("contactNumber");
  const address          = v("address");
  const dateOfBirth      = v("dateOfBirth") || null;   // LocalDate on server
  const emergencyContact = v("emergencyContact");

  /* Instructor fields */
  const licenseId               = v("license");
  const instructorContactNumber = v("instructorContactNumber");
  const experienceYears         = parseInt(v("experienceYears") || "0", 10);
  const vehicleType             = v("vehicleType");

  try {
    btn.disabled      = true;
    label.textContent = "Please wait…";

    /* ════════════════════════════════════════════
       SIGN UP
       POST /api/auth/register/student
       POST /api/auth/register/instructor
    ════════════════════════════════════════════ */
    if (currentMode === "signup") {
      if (!firstName || !lastName) {
        showError("Please enter your first and last name.");
        return;
      }
      if (password !== confirmPassword) {
        showError("Passwords do not match.");
        return;
      }

      const fullName = `${firstName} ${lastName}`.trim();

      /*
       * RegisterDTO.fullName is used for BOTH student and instructor.
       * AuthService.registerInstructor() calls registerDTO.getFullName()
       * and maps it to instructor.setInstructorName() — so we always
       * send "fullName", never "instructorName".
       *
       * workingDays: sent as empty array — can be set later in the
       * instructor dashboard once the profile exists.
       *
       * role: included so the server can validate if needed, though
       * AuthService currently doesn't read it from RegisterDTO.
       */
      const body = currentRole === "instructor"
          ? {
            fullName,
            email,
            password,
            role:          "INSTRUCTOR",
            licenseId,
            contactNumber: instructorContactNumber,
            experienceYears,
            vehicleType,
            workingDays:   []
          }
          : {
            fullName,
            email,
            password,
            role:          "STUDENT",
            contactNumber,
            nic,
            address,
            dateOfBirth,      // "YYYY-MM-DD" string — Spring deserialises to LocalDate
            emergencyContact
          };

      const endpoint = currentRole === "instructor"
          ? "/auth/register/instructor"
          : "/auth/register/student";

      await apiJson(endpoint, { method: "POST", body: JSON.stringify(body) });

      showSuccess("Account created! Please sign in.");
      currentMode = "signin";
      updateFormState();
      return;
    }

    /* ════════════════════════════════════════════
       SIGN IN  (student / instructor / admin)
       POST /api/auth/login
       LoginDTO: { email, password }
       — no role field; AuthService tries all three
         tables in order: Student → Instructor → Admin
    ════════════════════════════════════════════ */
    const loginData = await apiJson("/auth/login", {
      method: "POST",
      body:   JSON.stringify({ email, password })
    });

    /* loginData.content is LoginResponseDTO { userId, email, role } */
    const user = loginData.content;

    if (!user) {
      showError("Login failed — no user data returned.");
      return;
    }

    /* Persist minimal info for other pages (student/instructor/admin dashboards) */
    localStorage.setItem("fl-userId", user.userId);
    localStorage.setItem("fl-role",   user.role);
    localStorage.setItem("fl-email",  user.email);

    /* Redirect based on role the SERVER returned — not what the user clicked */
    switch (user.role?.toUpperCase()) {
      case "ADMIN":      window.location.href = "admin.html";      break;
      case "INSTRUCTOR": window.location.href = "instructor.html"; break;
      default:           window.location.href = "student.html";    break;
    }

  } catch (err) {
    /* apiJson throws when code !== "00" / response not ok */
    showError(err.message || "Something went wrong. Please try again.");
  } finally {
    btn.disabled = false;
    updateFormState();   // restores submit label
  }
}

/* ─── Logout — called from dashboard pages via window.AuthAPI.logout() ───── */

async function logout() {
  try {
    await apiJson("/auth/logout", { method: "POST" });
  } catch (_) {
    /* session may already be gone — swallow the error */
  } finally {
    localStorage.removeItem("fl-userId");
    localStorage.removeItem("fl-role");
    localStorage.removeItem("fl-email");
    window.location.href = "auth.html";
  }
}

/* ─── OAuth placeholder ──────────────────────────────────────────────────── */

function handleOAuth() {
  alert("Google login is not connected yet.");
}

/* ─── Alert helpers ──────────────────────────────────────────────────────── */

function showError(message) {
  renderAlert(message, "#fef2f2", "#b91c1c", "#fecaca");
}

function showSuccess(message) {
  renderAlert(message, "#f0fdf4", "#15803d", "#bbf7d0");
}

function renderAlert(message, bg, color, border) {
  clearAlert();
  const el = document.createElement("p");
  el.id = "authAlert";
  Object.assign(el.style, {
    margin: "0 0 14px", padding: "10px 14px",
    borderRadius: "8px", fontSize: "13px", lineHeight: "1.5",
    background: bg, color, border: `1px solid ${border}`
  });
  el.textContent = message;
  document.querySelector(".field-group")?.before(el);
}

function clearAlert() {
  document.getElementById("authAlert")?.remove();
}

/* ─── Tiny DOM helpers ───────────────────────────────────────────────────── */

function toggle(id, show) {
  document.getElementById(id)?.classList.toggle("hidden", !show);
}

function setText(id, text) {
  const el = document.getElementById(id);
  if (el) el.textContent = text;
}

function setHTML(id, html) {
  const el = document.getElementById(id);
  if (el) el.innerHTML = html;
}

/** Gets trimmed value of an input by id; returns "" if element missing */
function v(id) {
  return document.getElementById(id)?.value.trim() ?? "";
}

/* ─── Public API ─────────────────────────────────────────────────────────── */

window.AuthAPI = { logout };