/**
 * feedback.js
 * Depends on: api.js (loaded first in feedback.html)
 *
 * Wired endpoints:
 *   GET  /api/feedback/public    → public feedback wall (masonry grid)
 *   POST /api/feedback/submit    → submit feedback from the page form
 */

/* ─── Boot ─────────────────────────────────────────────────────────────── */

document.addEventListener("DOMContentLoaded", () => {
    loadPublicFeedback();
    loadCourseFilters();
    setupFeedbackForm();
    setupSort();
});

/* ─── Load public wall ──────────────────────────────────────────────────── */

let allFeedbacks = [];

async function loadPublicFeedback() {
    const masonry = document.querySelector(".masonry");
    if (!masonry) return;

    masonry.innerHTML = `<p class="muted" style="padding:16px;">Loading reviews…</p>`;

    try {
        const data = await apiJson("/feedback/public");
        allFeedbacks = data.content || [];

        if (!allFeedbacks.length) {
            masonry.innerHTML = `<p class="muted" style="padding:16px;">No reviews yet. Be the first!</p>`;
            return;
        }

        updateStats(allFeedbacks);
        renderWall(allFeedbacks);

    } catch (err) {
        masonry.innerHTML = `<p class="muted" style="padding:16px;">Could not load reviews.</p>`;
        console.warn("Could not load feedback:", err);
    }
}

function renderWall(feedbacks) {
    const masonry = document.querySelector(".masonry");
    if (!masonry) return;

    masonry.innerHTML = feedbacks.map((f, idx) => {
        const featured = idx === 0;
        return `
        <div class="review${featured ? " featured" : ""}">
            <div class="row-between">
                ${starsSVG(f.rating)}
                <span class="meta" style="font-family:var(--fl-font-mono);font-size:11px;letter-spacing:0.06em;">${formatDate(f.feedbackDate)}</span>
            </div>
            <p class="body serif" style="font-size:15px;line-height:1.55;margin:0;">"${f.comment || "—"}"</p>
            <div class="who">
                <span class="avatar-circle sm">${initialsFrom(f.studentName)}</span>
                <div>
                    <b>${f.studentName || "Anonymous"}</b>
                    <span>${f.instructorName ? `Instructor: ${f.instructorName}` : "FastLane Academy"}</span>
                </div>
            </div>
        </div>`;
    }).join("");
}

/* ─── Stats bar ─────────────────────────────────────────────────────────── */

function updateStats(feedbacks) {
    if (!feedbacks.length) return;

    const avg = feedbacks.reduce((s, f) => s + (f.rating || 0), 0) / feedbacks.length;

    // Overall rating big number
    const bigRating = document.querySelector(".rating-card .big");
    if (bigRating) {
        bigRating.innerHTML = `${avg.toFixed(1)}<small>/5</small>`;
    }

    // Rating bars (1-5 star breakdown)
    const bars = document.querySelectorAll(".rating-card .bar");
    for (let star = 5; star >= 1; star--) {
        const count = feedbacks.filter(f => f.rating === star).length;
        const pct   = Math.round((count / feedbacks.length) * 100);
        const bar   = bars[5 - star];
        if (!bar) continue;
        const track = bar.querySelector(".track > span");
        if (track) track.style.width = pct + "%";
    }

    // Stats band — total, this month, recommend %
    const now = new Date();
    const thisMonth = feedbacks.filter(f => {
        if (!f.feedbackDate) return false;
        const d = new Date(f.feedbackDate);
        return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth();
    }).length;

    const highRated = feedbacks.filter(f => (f.rating || 0) >= 4).length;
    const recommendPct = Math.round((highRated / feedbacks.length) * 100);

    const el = id => document.getElementById(id);
    if (el("stat-total"))     el("stat-total").textContent     = feedbacks.length.toLocaleString();
    if (el("stat-month"))     el("stat-month").textContent     = thisMonth;
    if (el("stat-recommend")) el("stat-recommend").textContent = recommendPct + "%";

    // Stats band numbers (legacy selectors)
    const statNums = document.querySelectorAll(".stats-band .s b");
    if (statNums[0]) statNums[0].textContent = avg.toFixed(1);
    if (statNums[1]) statNums[1].textContent = feedbacks.length;
}

/* ─── Course filter chips (loaded from DB) ──────────────────────────────── */

async function loadCourseFilters() {
    const bar = document.getElementById("wall-bar");
    if (!bar) return;

    try {
        const data = await apiJson("/courses/getAllCourses");
        const courses = (data.content || []).filter(c => c.status === "ACTIVE");

        // Insert one chip per course, before the .right div
        const right = bar.querySelector(".right");
        courses.forEach(c => {
            const btn = document.createElement("button");
            btn.className = "chip";
            btn.dataset.filter = String(c.courseId);
            btn.dataset.title = (c.courseTitle || "").toLowerCase();
            btn.textContent = c.courseTitle || `Course ${c.courseId}`;
            bar.insertBefore(btn, right);
        });

        setupFilters();
    } catch (err) {
        // If courses can't load, just leave the "All reviews" chip working
        setupFilters();
        console.warn("Could not load course filters:", err);
    }
}

/* ─── Filter chips ──────────────────────────────────────────────────────── */

function setupFilters() {
    document.querySelectorAll("#wall-bar .chip").forEach(chip => {
        chip.addEventListener("click", () => {
            document.querySelectorAll("#wall-bar .chip").forEach(c => c.classList.remove("active"));
            chip.classList.add("active");
            applyFiltersAndSort();
        });
    });
}

/* ─── Sort dropdown ─────────────────────────────────────────────────────── */

function setupSort() {
    document.getElementById("wall-sort")?.addEventListener("change", applyFiltersAndSort);
}

function applyFiltersAndSort() {
    const activeChip = document.querySelector("#wall-bar .chip.active");
    const filter = activeChip?.dataset.filter || "all";
    const sort = document.getElementById("wall-sort")?.value || "recent";

    let result = [...allFeedbacks];

    // Filter
    if (filter !== "all") {
        const title = activeChip?.dataset.title || "";
        result = result.filter(f =>
            String(f.courseId) === filter ||
            (f.courseName || "").toLowerCase().includes(title)
        );
    }

    // Sort
    if (sort === "top") {
        result.sort((a, b) => (b.rating || 0) - (a.rating || 0));
    } else {
        result.sort((a, b) => new Date(b.feedbackDate) - new Date(a.feedbackDate));
    }

    renderWall(result);
}

/* ─── Submit form ───────────────────────────────────────────────────────── */

function setupFeedbackForm() {
    const form = document.querySelector("#submit form, #submit, form.feedback-form");
    if (!form) return;

    form.addEventListener("submit", async e => {
        e.preventDefault();
        await submitFeedback();
    });

    // Also wire the standalone submit button on the form
    const submitBtn = document.querySelector("#submit .btn--primary, .feedback-form .btn--primary");
    if (submitBtn) {
        submitBtn.addEventListener("click", async e => {
            e.preventDefault();
            await submitFeedback();
        });
    }
}

async function submitFeedback() {
    const userId = localStorage.getItem("fl-userId");

    const rating = document.querySelectorAll(".rating-input button.on, [data-stars] svg.on").length;
    const comment      = document.querySelector("#fb-comment, textarea[name='comment']")?.value || "";
    const instructorId = document.querySelector("#fb-instructor, select[name='instructor']")?.value || null;
    const studentName  = document.querySelector("#fb-name, input[name='name']")?.value || "Anonymous";

    if (!rating) {
        showToast("Please select a star rating.", false);
        return;
    }
    if (!comment.trim()) {
        showToast("Please write a short comment.", false);
        return;
    }

    try {
        await apiJson("/feedback/submit", {
            method: "POST",
            body: JSON.stringify({
                studentId: userId || null,
                studentName,
                instructorId,
                rating,
                comment
            })
        });

        showToast("Review submitted. Thank you!");

        // Clear form
        document.querySelectorAll("[data-stars] svg, .rating-input button").forEach(s => s.classList.remove("on"));
        const textarea = document.querySelector("#fb-comment, textarea[name='comment']");
        if (textarea) textarea.value = "";

        // Reload wall
        await loadPublicFeedback();

    } catch (err) {
        showToast(err.message || "Could not submit review.", false);
    }
}

/* ─── Helpers ───────────────────────────────────────────────────────────── */

function initialsFrom(name) {
    if (!name) return "?";
    return name.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase();
}

function formatDate(dateStr) {
    if (!dateStr) return "—";
    try { return new Date(dateStr).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }); }
    catch { return dateStr; }
}

function starsSVG(count) {
    return `<div class="stars">${Array.from({ length: 5 }, (_, i) => {
        const on = i < count;
        return `<svg width="14" height="14" viewBox="0 0 24 24"
            fill="${on ? "#97B154" : "none"}" stroke="${on ? "#97B154" : "#d9d9d6"}">
            <polygon points="12 2 15 9 22 9.3 17 14 18.5 21 12 17.3 5.5 21 7 14 2 9.3 9 9 12 2"/>
        </svg>`;
    }).join("")}</div>`;
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
    t._timer = setTimeout(() => { t.style.opacity = "0"; }, 3000);
}