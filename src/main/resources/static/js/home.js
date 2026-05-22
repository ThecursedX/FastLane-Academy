document.addEventListener("DOMContentLoaded", () => {
    loadHomeInstructors();
    loadHomeCourses();
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

async function loadHomeCourses() {
    const grid = document.getElementById("home-course-grid");
    if (!grid) return;

    try {
        const data = await apiJson("/courses/getAllCourses");
        const courses = (data.content || []).filter(c => c.status === "ACTIVE");

        if (!courses.length) {
            grid.innerHTML = `<p class="muted" style="padding:16px;grid-column:1/-1;">No courses available right now.</p>`;
            return;
        }

        const checkSVG = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>`;

        grid.innerHTML = courses.map((c, idx) => {
            const isFeat = idx === 1;
            const glyph = (c.courseTitle || "?")[0].toUpperCase();
            const priceDisplay = c.price != null
                ? `<div class="price-amt">$<em>${c.price}</em><small>Per full course</small></div>`
                : "";
            const duration = c.durationHours ? `${c.durationHours}h total` : "";
            const level = c.difficultyLevel || "";

            const featureLines = [
                duration ? `${duration} of instruction` : null,
                level ? `${level} level` : null,
                c.description || null
            ].filter(Boolean).slice(0, 3);

            return `
            <div class="price${isFeat ? " feat" : ""}">
                <span class="lbl">${level}${level && duration ? " · " : ""}${duration}</span>
                <div class="cat">${glyph}</div>
                <h3>${c.courseTitle || "—"}</h3>
                ${priceDisplay}
                <hr>
                <ul>
                    ${featureLines.map(f => `<li>${checkSVG} ${f}</li>`).join("")}
                </ul>
                <a href="courses.html" class="btn ${isFeat ? "btn--primary" : "btn--ink"} btn--block">View course</a>
            </div>`;
        }).join("");

    } catch (err) {
        console.warn("Could not load courses for home page:", err);
    }
}
