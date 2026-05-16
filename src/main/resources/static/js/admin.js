document.addEventListener("DOMContentLoaded", () => {
    setupAdminNavigation();
    setupAdminTheme();
    loadCourses();
    loadInstructors();
});

const paneTitles = {
    overview: { crumbs: "Console · May 2026", title: "FastLane operations" },
    students: { crumbs: "People · Enrolment", title: "Students" },
    payments: { crumbs: "Finance · Transactions", title: "Payments & billing" },
    courses: { crumbs: "Catalogue · Pricing", title: "Courses" },
    instructors: { crumbs: "People · Staff", title: "Instructors" },
    feedback: { crumbs: "Reviews · Inbox", title: "Feedback" }
};

function setupAdminNavigation() {
    document.querySelectorAll("[data-pane]").forEach(a => {
        a.addEventListener("click", e => {
            e.preventDefault();

            const name = a.dataset.pane;

            document.querySelectorAll(".dash__nav a[data-pane]").forEach(x => {
                x.classList.toggle("active", x.dataset.pane === name);
            });

            document.querySelectorAll(".pane").forEach(p => {
                p.classList.toggle("active", p.id === "pane-" + name);
            });

            const crumbs = document.querySelector(".dash__bar .crumbs");
            const title = document.getElementById("pageTitle");

            if (crumbs) crumbs.textContent = paneTitles[name].crumbs;
            if (title) title.textContent = paneTitles[name].title;

            if (name === "courses") loadCourses();
            if (name === "instructors") loadInstructors();

            window.scrollTo({ top: 0, behavior: "smooth" });
        });
    });
}

function setupAdminTheme() {
    const t = localStorage.getItem("fl-theme") || "light";
    document.documentElement.dataset.theme = t;

    const moon = document.getElementById("dashMoon");
    const sun = document.getElementById("dashSun");

    if (moon && sun) {
        moon.style.display = t === "dark" ? "none" : "block";
        sun.style.display = t === "dark" ? "block" : "none";
    }

    document.addEventListener("keydown", e => {
        if (e.altKey && e.key === "d") toggleThemeNav();
    });
}

function toggleThemeNav() {
    const html = document.documentElement;
    const next = html.dataset.theme === "dark" ? "light" : "dark";

    html.dataset.theme = next;
    localStorage.setItem("fl-theme", next);

    const moon = document.getElementById("dashMoon");
    const sun = document.getElementById("dashSun");

    if (moon && sun) {
        moon.style.display = next === "dark" ? "none" : "block";
        sun.style.display = next === "dark" ? "block" : "none";
    }
}

function showToast(msg, ok = true) {
    let t = document.getElementById("fl-toast");

    if (!t) {
        t = document.createElement("div");
        t.id = "fl-toast";
        t.style.cssText = "position:fixed;bottom:24px;right:24px;z-index:9999;padding:12px 20px;border-radius:10px;font-size:14px;font-weight:500;box-shadow:0 4px 24px rgba(0,0,0,0.18);transition:opacity 0.3s;";
        document.body.appendChild(t);
    }

    t.textContent = msg;
    t.style.background = ok ? "var(--fl-moss-700)" : "#c0392b";
    t.style.color = "#fff";
    t.style.opacity = "1";

    clearTimeout(t._timer);
    t._timer = setTimeout(() => {
        t.style.opacity = "0";
    }, 3000);
}

function difficultyBadge(level) {
    const map = {
        BEGINNER: "badge--moss",
        INTERMEDIATE: "badge--warn",
        ADVANCED: "badge--error"
    };

    return `<span class="badge ${map[level] || "badge--neutral"}">${level || "—"}</span>`;
}

function statusBadge(status) {
    return status === "ACTIVE"
        ? `<span class="badge badge--moss"><span class="dot"></span>Live</span>`
        : `<span class="badge badge--neutral"><span class="dot"></span>Inactive</span>`;
}

function initials(name) {
    if (!name) return "??";
    return name.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase();
}

function toneClass(i) {
    const tones = ["ink", "tone-b", "tone-c", "tone-d", "tone-e", ""];
    return tones[i % tones.length];
}

async function loadCourses() {
    const container = document.querySelector("#pane-courses .card.mb-8");
    if (!container) return;

    try {
        const data = await apiJson("/courses/getAllCourses");
        const courses = data.content || data;

        container.querySelectorAll(".course-row").forEach(r => r.remove());

        courses.forEach(c => {
            const row = document.createElement("div");
            row.className = "course-row";
            row.dataset.courseId = c.courseId;

            row.innerHTML = `
                <div class="glyph">${(c.courseTitle || "?")[0].toUpperCase()}</div>
                <div>
                    <h4>${c.courseTitle || "—"}</h4>
                    <div class="meta">${c.durationHours || 0}h · ${c.difficultyLevel || "—"}</div>
                </div>
                <div><div class="eyebrow">Difficulty</div>${difficultyBadge(c.difficultyLevel)}</div>
                <div><div class="eyebrow">Status</div>${statusBadge(c.status)}</div>
                <div><div class="eyebrow">ID</div><b>${c.courseId}</b></div>
            `;

            container.appendChild(row);
        });

    } catch (error) {
        showToast("Could not load courses", false);
        console.warn(error);
    }
}

async function loadInstructors() {
    const grid = document.querySelector("#pane-instructors .inst-grid-admin");
    if (!grid) return;

    try {
        const data = await apiJson("/instructors/getAllInstructors");
        const instructors = data.content || data;

        grid.innerHTML = "";

        instructors.forEach((inst, idx) => {
            const card = document.createElement("div");
            card.className = "inst-card-admin";

            card.innerHTML = `
                <div class="top">
                    <span class="avatar-circle lg ${toneClass(idx)}">${initials(inst.instructorName)}</span>
                    <div>
                        <h4>${inst.instructorName || "—"}</h4>
                        <div class="role">${inst.vehicleType || "—"} · ${inst.experienceYears || 0}yr exp</div>
                    </div>
                    ${inst.status === "ACTIVE"
                ? `<span class="badge badge--moss" style="margin-left:auto;"><span class="dot"></span>Active</span>`
                : `<span class="badge badge--neutral" style="margin-left:auto;"><span class="dot"></span>Inactive</span>`}
                </div>
                <div class="row3">
                    <div><b>${inst.experienceYears || 0}</b><span>Yrs exp</span></div>
                    <div><b>${inst.licenseId || "—"}</b><span>Licence</span></div>
                    <div><b>${(inst.workingDays || []).length}</b><span>Days/wk</span></div>
                </div>
            `;

            grid.appendChild(card);
        });

    } catch (error) {
        showToast("Could not load instructors", false);
        console.warn(error);
    }
}