document.addEventListener("DOMContentLoaded", () => {
    applyHomeTheme();
    loadHomeInstructors();
});

function applyHomeTheme() {
    const theme = localStorage.getItem("fl-theme") || "light";
    document.documentElement.dataset.theme = theme;

    const moon = document.getElementById("navMoon");
    const sun = document.getElementById("navSun");

    if (moon && sun) {
        moon.style.display = theme === "dark" ? "none" : "block";
        sun.style.display = theme === "dark" ? "block" : "none";
    }
}

function toggleThemeNav() {
    const html = document.documentElement;
    const next = html.dataset.theme === "dark" ? "light" : "dark";

    html.dataset.theme = next;
    localStorage.setItem("fl-theme", next);

    const moon = document.getElementById("navMoon");
    const sun = document.getElementById("navSun");

    if (moon && sun) {
        moon.style.display = next === "dark" ? "none" : "block";
        sun.style.display = next === "dark" ? "block" : "none";
    }
}

document.addEventListener("keydown", e => {
    if (e.altKey && e.key === "d") toggleThemeNav();
});

function initialsFromName(name) {
    if (!name) return "??";
    return name.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase();
}

async function loadHomeInstructors() {
    const grid = document.querySelector(".inst-grid");
    if (!grid) return;

    try {
        const data = await apiJson("/instructors/studentView");
        const instructors = data.content || data;

        if (!instructors || !instructors.length) return;

        const colorClasses = ["a", "b", "c", "d", "e"];
        grid.innerHTML = "";

        instructors.forEach((inst, idx) => {
            const cls = colorClasses[idx % colorClasses.length];
            const days = (inst.workingDays || [])
                .map(d => d[0] + d.slice(1).toLowerCase())
                .join(", ") || "Flexible";

            const article = document.createElement("article");
            article.className = `inst-card ${cls}`;

            article.innerHTML = `
                <div class="face">
                    <div class="silhouette"></div>
                    <div class="initials">${initialsFromName(inst.instructorName)}</div>
                    <div class="stat-pill"><b>${inst.experienceYears || 0}yr</b>Experience</div>
                </div>
                <div class="body">
                    <div class="role">${inst.vehicleType || "Instructor"}</div>
                    <h3>${inst.instructorName || "Instructor"}</h3>
                    <p class="bio">Licence: ${inst.licenseId || "—"} · ${inst.experienceYears || 0} years on the road.</p>
                    <div class="tags">
                        <span>${inst.vehicleType || "—"}</span>
                        <span>${days}</span>
                    </div>
                </div>
            `;

            grid.appendChild(article);
        });

    } catch (error) {
        console.warn("Could not load instructors:", error);
    }
}