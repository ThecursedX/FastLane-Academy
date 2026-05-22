/**
 * courses.js
 * Depends on: api.js (loaded first in courses.html)
 *
 * Wired endpoints:
 *   GET /api/courses/getAllCourses   → public course catalog grid
 */

/* ─── Boot ─────────────────────────────────────────────────────────────── */

document.addEventListener("DOMContentLoaded", () => {
    loadPublicCourses();
});

/* ─── Load & render courses ─────────────────────────────────────────────── */

let allCourses = [];

async function loadPublicCourses() {
    const grid = document.querySelector(".course-grid");
    if (!grid) return;

    grid.innerHTML = skeletonCards(4);

    try {
        const data = await apiJson("/courses/getAllCourses");
        allCourses = (data.content || []).filter(c => c.status === "ACTIVE");

        if (!allCourses.length) {
            grid.innerHTML = `<p class="muted" style="padding:16px;">No courses available right now.</p>`;
            return;
        }

        renderCourses(allCourses);

    } catch (err) {
        grid.innerHTML = `<p class="muted" style="padding:16px;">Could not load courses.</p>`;
        console.warn("Could not load courses:", err);
    }
}

function renderCourses(courses) {
    const grid = document.querySelector(".course-grid");
    if (!grid) return;

    grid.innerHTML = courses.map(c => `
        <div class="course" data-course-id="${c.courseId}" data-difficulty="${c.difficultyLevel || ""}">
            <div>
                <div class="lbl">${c.difficultyLevel || "Course"} · ${c.durationHours || 0}h</div>
                <h3>${c.courseTitle || "—"}</h3>
                <p class="one-line">${c.description || "Contact us for more details."}</p>
                <div class="meta-row">
                    <div class="m"><span>Duration</span><b>${c.durationHours || 0}h</b></div>
                    <div class="m"><span>Level</span><b>${c.difficultyLevel || "—"}</b></div>
                    <div class="m"><span>Status</span><b>${c.status || "—"}</b></div>
                    <div class="m"><span>ID</span><b style="font-size:14px;">${c.courseId || "—"}</b></div>
                </div>
                <a href="auth.html" class="btn btn--primary">Enrol now →</a>
            </div>
            <div class="cat-glyph">${(c.courseTitle || "?")[0].toUpperCase()}</div>
        </div>
    `).join("");
}

/* ─── Filter chips ──────────────────────────────────────────────────────── */

function setupFilters() {
    document.querySelectorAll(".ph__bar .chip").forEach(chip => {
        chip.addEventListener("click", () => {
            document.querySelectorAll(".ph__bar .chip").forEach(c => c.classList.remove("active"));
            chip.classList.add("active");

            const filter = chip.textContent.trim().toLowerCase();

            if (filter === "all courses" || filter === "all") {
                renderCourses(allCourses);
                return;
            }

            const filtered = allCourses.filter(c => {
                const diff = (c.difficultyLevel || "").toLowerCase();
                const title = (c.courseTitle || "").toLowerCase();
                return diff.includes(filter) || title.includes(filter);
            });

            renderCourses(filtered);
        });
    });
}

/* ─── Skeleton loader ───────────────────────────────────────────────────── */

function skeletonCards(n) {
    return Array.from({ length: n }, () => `
        <div class="course" style="opacity:0.4;pointer-events:none;">
            <div>
                <div class="lbl" style="background:var(--fl-ink-100);height:12px;width:80px;border-radius:4px;margin-bottom:12px;"></div>
                <div style="background:var(--fl-ink-100);height:28px;width:200px;border-radius:4px;margin-bottom:10px;"></div>
                <div style="background:var(--fl-ink-50);height:14px;width:100%;border-radius:4px;margin-bottom:6px;"></div>
                <div style="background:var(--fl-ink-50);height:14px;width:60%;border-radius:4px;"></div>
            </div>
        </div>
    `).join("");
}