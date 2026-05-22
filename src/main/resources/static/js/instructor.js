/**
 * instructor.js  — Depends on api.js (loaded first)
 *
 * New flow:
 *   1. Load profile
 *   2. Load my availability slots  GET /api/slots/mySlots
 *   3. Create slot                 POST /api/slots/create    { dayOfWeek, time }
 *   4. Enable / Disable slot       PUT  /api/slots/enable/{id}  | /disable/{id}
 *   5. Delete slot                 DELETE /api/slots/delete/{id}
 *   6. View requests per slot      GET /api/slots/{id}/requests
 *   7. Mark lesson complete/cancel PUT  /api/lessons/updateStatus/{lessonId}?status=
 *   8. Feedback, profile save (unchanged)
 */

document.addEventListener("DOMContentLoaded", () => {
    const userId = localStorage.getItem("fl-userId");
    const role   = localStorage.getItem("fl-role");
    if (!userId || role !== "INSTRUCTOR") { window.location.href = "auth.html"; return; }

    loadInstructorProfile(userId);
    loadMySlots(userId);
    loadInstructorFeedback(userId);
    setupProfileSave(userId);
    setupSlotForm();
    setupLogout();
});

/* ─── Profile ──────────────────────────────────────────────────────────── */

async function loadInstructorProfile(userId) {
    try {
        const data = await apiJson(`/instructors/getInstructor/${userId}`);
        const inst = data.content;
        if (!inst) return;
        const initials = initialsFrom(inst.instructorName);
        document.querySelectorAll(".avatar-circle:not(.xl)").forEach(el => el.textContent = initials);
        const whoName = document.querySelector(".dash__profile .who b");
        const whoSub  = document.querySelector(".dash__profile .who span");
        if (whoName) whoName.textContent = inst.instructorName || "—";
        if (whoSub)  whoSub.textContent  = `${inst.vehicleType || "Instructor"} · ${inst.experienceYears || 0}yr`;
        const pageTitle = document.getElementById("pageTitle");
        if (pageTitle) pageTitle.textContent = `Good morning, ${firstName(inst.instructorName)}.`;
        const heroLbl = document.querySelector(".hero-card .lbl");
        if (heroLbl) heroLbl.textContent = `${inst.vehicleType || "—"} instructor · Licence ${inst.licenseId || "—"}`;
        const heroP = document.querySelector(".hero-card p");
        if (heroP) heroP.textContent = `${inst.experienceYears || 0} years on the road. ${formatDays(inst.workingDays)} available.`;
        const avatarXl = document.querySelector(".avatar-circle.xl");
        if (avatarXl) avatarXl.textContent = initials;
        const profileH2 = document.querySelector("#pane-profile h2");
        if (profileH2) profileH2.textContent = inst.instructorName || "—";
        fillInput("inst-name-field",       inst.instructorName);
        fillInput("inst-email-field",      inst.email);
        fillInput("inst-phone-field",      inst.contactNumber);
        fillInput("inst-license-field",    inst.licenseId);
        fillInput("inst-experience-field", inst.experienceYears);
        fillInput("inst-vehicle-field",    inst.vehicleType);
        const days = inst.workingDays || [];
        document.querySelectorAll(".pref-cell[data-day]").forEach(cell => {
            cell.classList.toggle("on", days.includes(cell.dataset.day?.toUpperCase()));
        });
    } catch (err) { console.warn("Could not load profile:", err); }
}

/* ─── Availability Slots ───────────────────────────────────────────────── */

async function loadMySlots(userId) {
    try {
        const data = await apiJson(`/slots/mySlots?instructorId=${userId}`);
        const slots = data.content || [];
        renderSlots(slots);
        updateSlotStats(slots);
    } catch (err) { console.warn("Could not load slots:", err); }
}

function updateSlotStats(slots) {
    const total   = slots.length;
    const enabled = slots.filter(s => s.enabled).length;
    const waiting = slots.reduce((sum, s) => sum + (s.activeRequestCount || 0), 0);
    const el = id => document.getElementById(id);
    if (el("st-total"))   el("st-total").textContent   = total;
    if (el("st-open"))    el("st-open").textContent    = enabled;
    if (el("st-booked"))  el("st-booked").textContent  = waiting + " waiting";
    if (el("hero-today")) el("hero-today").textContent = enabled;
    if (el("hero-today-sub")) el("hero-today-sub").textContent = `${total} slot${total !== 1 ? "s" : ""} configured`;
}

function renderSlots(slots) {
    const grid = document.getElementById("slots-grid");
    if (!grid) return;

    if (!slots.length) {
        grid.innerHTML = `<div class="empty" style="grid-column:1/-1;padding:60px;">
            <p>No availability slots yet. Add one to get started.</p>
        </div>`;
        return;
    }

    // Sort: day of week order, then time
    const dayOrder = ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"];
    slots.sort((a, b) => {
        const di = dayOrder.indexOf(a.dayOfWeek) - dayOrder.indexOf(b.dayOfWeek);
        if (di !== 0) return di;
        return a.time.localeCompare(b.time);
    });

    grid.innerHTML = slots.map(s => {
        const hasQueue = s.activeRequestCount > 0;
        return `
        <div class="slot-card${s.enabled ? " taken" : ""}" data-slot-id="${s.slotId}">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;">
                <div>
                    <div class="info">${s.dayOfWeek}</div>
                    <div class="dt">${formatTime(s.time)}</div>
                </div>
                <span class="badge ${s.enabled ? "badge--moss" : "badge--neutral"}">
                    <span class="dot"></span>${s.enabled ? "Enabled" : "Disabled"}
                </span>
            </div>
            ${hasQueue ? `
            <div class="student-chip">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                <span><b>${s.activeRequestCount}</b> student${s.activeRequestCount !== 1 ? "s" : ""} waiting</span>
            </div>` : `<div style="font-size:12px;color:var(--fl-fg-faint);">No students queued</div>`}
            <div style="display:flex;gap:6px;flex-wrap:wrap;margin-top:4px;">
                ${s.enabled
                    ? `<button class="btn btn--ghost btn--sm" onclick="toggleSlot('${s.slotId}', false)">Disable</button>`
                    : `<button class="btn btn--primary btn--sm" onclick="toggleSlot('${s.slotId}', true)">Enable + run FIFO</button>`}
                <button class="btn btn--ghost btn--sm" onclick="viewRequests('${s.slotId}', '${s.dayOfWeek}', '${s.time}')">View queue</button>
                ${!hasQueue
                    ? `<button class="btn btn--ghost btn--sm" style="color:var(--fl-error);" onclick="deleteSlot('${s.slotId}')">Delete</button>`
                    : ""}
            </div>
        </div>`;
    }).join("");
}

async function toggleSlot(slotId, enable) {
    try {
        const action = enable ? "enable" : "disable";
        const res = await apiJson(`/slots/${action}/${slotId}`, { method: "PUT" });
        showToast(enable ? "Slot enabled — FIFO selection complete." : (res.message || "Slot disabled."));
        loadMySlots(localStorage.getItem("fl-userId"));
    } catch (err) { showToast(err.message || "Could not update slot.", false); }
}

async function deleteSlot(slotId) {
    if (!confirm("Delete this slot? This cannot be undone.")) return;
    try {
        await apiJson(`/slots/delete/${slotId}`, { method: "DELETE" });
        showToast("Slot deleted.");
        loadMySlots(localStorage.getItem("fl-userId"));
    } catch (err) { showToast(err.message || "Could not delete slot.", false); }
}

async function viewRequests(slotId, day, time) {
    try {
        // Store slot context on modal for re-open after Complete/Cancel
        const modalEl = document.getElementById("modal-queue");
        if (modalEl) { modalEl._slotId = slotId; modalEl._day = day; modalEl._time = time; }

        const data = await apiJson(`/slots/${slotId}/requests`);
        const requests = data.content || [];
        renderQueueModal(requests, day, time);
        openModal("modal-queue");
    } catch (err) { showToast(err.message || "Could not load queue.", false); }
}

function renderQueueModal(requests, day, time) {
    const titleEl = document.getElementById("queue-modal-title");
    if (titleEl) titleEl.textContent = `${day} at ${formatTime(time)}`;

    const listEl = document.getElementById("queue-list");
    if (!listEl) return;

    if (!requests.length) {
        listEl.innerHTML = `<p class="muted" style="padding:12px 0;">No requests for this slot.</p>`;
        return;
    }

    // --- FIX 1: Filter out past dates — only today and future ---
    const todayStr = new Date().toISOString().slice(0, 10);
    const futureRequests = requests.filter(r => r.date >= todayStr);

    if (!futureRequests.length) {
        listEl.innerHTML = `<p class="muted" style="padding:12px 0;">No upcoming requests for this slot.</p>`;
        return;
    }

    const statusOrder = { SELECTED: 0, IN_QUEUE: 1, SLOT_TAKEN: 2, PENDING: 3, DISABLED: 4, CANCELLED: 5, COMPLETED: 6 };
    const statusLabels = { SELECTED: "Selected", IN_QUEUE: "In queue", SLOT_TAKEN: "Slot taken", PENDING: "Pending", CANCELLED: "Cancelled", COMPLETED: "Completed", DISABLED: "Slot disabled" };
    const statusClasses = { SELECTED: "badge--moss", IN_QUEUE: "badge--neutral", SLOT_TAKEN: "badge--neutral", PENDING: "badge--warn", CANCELLED: "badge--danger", COMPLETED: "badge--moss", DISABLED: "badge--danger" };

    // Group by date
    const byDate = {};
    futureRequests.forEach(r => { if (!byDate[r.date]) byDate[r.date] = []; byDate[r.date].push(r); });
    const sortedDates = Object.keys(byDate).sort();

    listEl.innerHTML = sortedDates.map(date => {
        const group = byDate[date].sort((a, b) =>
            ((statusOrder[a.status] ?? 9) - (statusOrder[b.status] ?? 9)) || new Date(a.requestedAt) - new Date(b.requestedAt)
        );

        // --- FIX 2: If any request has lessonStatus === COMPLETED → show "✓ Already Complete", no rows ---
        const hasCompleted = group.some(r => r.lessonStatus === "COMPLETED");
        const formattedDate = formatDate(date);

        if (hasCompleted) {
            return `<div style="margin-bottom:18px;">
                <div style="display:flex;align-items:center;gap:10px;padding:10px 12px;background:var(--fl-bg-elev,#f9fafb);border-radius:10px;">
                    <span style="font-size:15px;">✅</span>
                    <span style="font-size:15px;font-weight:700;">${formattedDate}</span>
                    <span class="badge badge--moss" style="font-size:10px;margin-left:auto;">✓ Already Complete</span>
                </div>
            </div>`;
        }

        let qPos = 0;
        const rows = group.map(r => {
            const isActive = r.status !== "CANCELLED";
            if (isActive) qPos++;
            return `
            <div class="srow">
                <div style="font-size:12px;color:var(--fl-fg-faint);font-family:var(--fl-font-mono);">${isActive ? `#${qPos}` : "—"}</div>
                <div>
                    <b>${r.studentId}</b>
                    <div class="meta">${r.courseId} · ${formatDate(r.date)} · queued ${formatDateTime(r.requestedAt)}</div>
                    ${r.lessonId ? `<div class="meta" style="color:var(--fl-moss-600);">Lesson: ${r.lessonId}</div>` : ""}
                </div>
                <span class="badge ${statusClasses[r.status] || "badge--neutral"}"><span class="dot"></span>${statusLabels[r.status] || r.status}</span>
                ${r.status === "SELECTED" && r.lessonId
                    ? `<div style="display:flex;gap:6px;">
                         <button class="btn btn--primary btn--sm" onclick="markLessonDone('${r.lessonId}')">Complete</button>
                         <button class="btn btn--ghost btn--sm" onclick="markLessonCancelled('${r.lessonId}')">Cancel</button>
                       </div>`
                    : `<div></div>`}
            </div>`;
        }).join("");

        const activeCount = group.filter(r => r.status !== "CANCELLED").length;
        return `<div style="margin-bottom:18px;">
            <div style="display:flex;align-items:center;gap:10px;padding:10px 12px;background:var(--fl-bg-elev,#f9fafb);border-radius:10px;margin-bottom:6px;">
                <span style="font-size:15px;">📅</span>
                <span style="font-size:15px;font-weight:700;">${formattedDate}</span>
                <span class="badge ${activeCount > 0 ? 'badge--moss' : 'badge--neutral'}" style="font-size:10px;margin-left:auto;">${activeCount} active</span>
            </div>
            ${rows}
        </div>`;
    }).join("");
}

async function markLessonDone(lessonId) {
    if (!lessonId) return;
    try {
        await apiJson(`/lessons/updateStatus/${lessonId}?status=COMPLETED`, { method: "PUT" });
        showToast("Lesson marked as completed.");
        // --- FIX 3: Close → refresh slots → re-open modal showing updated state ---
        closeModal("modal-queue");
        const userId = localStorage.getItem("fl-userId");
        await loadMySlots(userId);
        const modalEl = document.getElementById("modal-queue");
        if (modalEl && modalEl._slotId) {
            viewRequests(modalEl._slotId, modalEl._day, modalEl._time);
        }
    } catch (err) { showToast(err.message || "Could not update lesson.", false); }
}

async function markLessonCancelled(lessonId) {
    if (!lessonId || !confirm("Cancel this lesson?")) return;
    try {
        await apiJson(`/lessons/updateStatus/${lessonId}?status=CANCELLED`, { method: "PUT" });
        showToast("Lesson cancelled.");
        closeModal("modal-queue");
        const userId = localStorage.getItem("fl-userId");
        await loadMySlots(userId);
        const modalEl = document.getElementById("modal-queue");
        if (modalEl && modalEl._slotId) {
            viewRequests(modalEl._slotId, modalEl._day, modalEl._time);
        }
    } catch (err) { showToast(err.message || "Could not cancel lesson.", false); }
}

/* ─── Course dropdown for slot modal ──────────────────────────────────── */

async function loadCoursesDropdown() {
    const sel = document.getElementById("sl-course");
    if (!sel) return;
    sel.innerHTML = '<option value="">Loading courses...</option>';
    try {
        const d = await apiJson("/courses/getAllCourses");
        const courses = (d.content || d || []).filter(c => c.status === "ACTIVE");
        if (!courses.length) { sel.innerHTML = '<option value="">No active courses found</option>'; return; }
        sel.innerHTML = '<option value="">Select a course...</option>' +
            courses.map(c => `<option value="${c.courseId}">${c.courseTitle} (${c.courseId})</option>`).join("");
    } catch (e) {
        sel.innerHTML = '<option value="">Could not load courses</option>';
    }
}

/* ─── Create slot form ─────────────────────────────────────────────────── */

function setupSlotForm() {
    const btn = document.getElementById("add-slot-btn");
    if (!btn) return;
    btn.addEventListener("click", submitAddSlot);
}

async function submitAddSlot() {
    const alertEl  = document.getElementById("slot-alert");
    const courseId = val("sl-course");
    const dayOfWeek = val("sl-day");
    const time      = val("sl-time");

    if (!courseId || !dayOfWeek || !time) {
        showInlineAlert(alertEl, "Please select a course, day and time.", "error");
        return;
    }

    const userId = localStorage.getItem("fl-userId");
    const btn = document.getElementById("add-slot-btn");
    btn.disabled = true; btn.textContent = "Creating…";

    try {
        await apiJson("/slots/create", {
            method: "POST",
            body: JSON.stringify({ courseId, dayOfWeek, time, instructorId: userId })
        });
        showInlineAlert(alertEl, "Slot created. Enable it when you're ready.", "success");
        document.getElementById("sl-course").value = "";
        document.getElementById("sl-day").value    = "";
        document.getElementById("sl-time").value   = "";
        loadMySlots(userId);
        setTimeout(() => closeModal("modal-add-slot"), 1600);
    } catch (err) {
        showInlineAlert(alertEl, err.message || "Could not create slot.", "error");
    } finally {
        btn.disabled = false; btn.textContent = "Add slot";
    }
}

/* ─── Feedback ─────────────────────────────────────────────────────────── */

async function loadInstructorFeedback(userId) {
    try {
        const lessonsData = await apiJson(`/lessons/getLessonsByInstructorId/${userId}`);
        const lessons     = lessonsData.content || [];
        const courseIds   = [...new Set(lessons.map(l => l.courseId).filter(Boolean))];
        const allFeedback = [];
        await Promise.all(courseIds.map(async cId => {
            try { const d = await apiJson(`/feedback/byCourse/${cId}`); (d.content || []).forEach(f => allFeedback.push(f)); } catch (_) {}
        }));
        const grid = document.querySelector("#pane-profile .grid-3");
        if (!grid || !allFeedback.length) return;
        grid.innerHTML = allFeedback.slice(0, 3).map(f => `
            <div style="background:var(--fl-ink-50);border-radius:14px;padding:20px;">
                <div class="stars mb-2">${starsSVG(f.rating)}</div>
                <p class="serif" style="font-size:14px;line-height:1.5;margin:0 0 12px;">"${f.comment || "—"}"</p>
                <div class="muted mono" style="font-size:11px;letter-spacing:0.08em;">— ${f.studentId || "Student"} · ${formatDate(f.feedbackDate)}</div>
            </div>`).join("");
    } catch (err) { console.warn("Could not load feedback:", err); }
}

/* ─── Profile save ─────────────────────────────────────────────────────── */

function setupProfileSave(userId) {
    document.addEventListener("click", async e => {
        const btn = e.target.closest("#pane-profile .btn--primary");
        if (!btn) return;
        const selectedDays = Array.from(document.querySelectorAll(".pref-cell.on[data-day]"))
            .map(el => el.dataset.day.toUpperCase());
        const body = {
            instructorId:    userId,
            instructorName:  val("inst-name-field"),
            email:           val("inst-email-field"),
            contactNumber:   val("inst-phone-field"),
            licenseId:       val("inst-license-field"),
            experienceYears: parseInt(val("inst-experience-field") || "0"),
            vehicleType:     val("inst-vehicle-field"),
            workingDays:     selectedDays
        };
        try {
            await apiJson(`/instructors/update/${userId}`, { method: "PUT", body: JSON.stringify(body) });
            showToast("Profile saved.");
        } catch (err) { showToast(err.message || "Could not save profile.", false); }
    });
    document.querySelectorAll(".pref-cell[data-day]").forEach(cell => {
        cell.addEventListener("click", () => cell.classList.toggle("on"));
    });
}

/* ─── Logout ───────────────────────────────────────────────────────────── */

function setupLogout() {
    document.querySelector("a[href='auth.html'].icon-btn")
        ?.addEventListener("click", async e => {
            e.preventDefault();
            try { await apiJson("/auth/logout", { method: "POST" }); } catch (_) {}
            ["fl-userId", "fl-role", "fl-email"].forEach(k => localStorage.removeItem(k));
            window.location.href = "auth.html";
        });
}

/* ─── Helpers ──────────────────────────────────────────────────────────── */

function requestStatusBadge(status) {
    const map = {
        PENDING:   "badge--warn",
        IN_QUEUE:  "badge--neutral",
        SELECTED:  "badge--moss",
        CANCELLED: "badge--danger"
    };
    const labels = { PENDING: "Pending", IN_QUEUE: "In queue", SELECTED: "Selected", CANCELLED: "Cancelled" };
    return `<span class="badge ${map[status] || "badge--neutral"}"><span class="dot"></span>${labels[status] || status}</span>`;
}

function initialsFrom(name) {
    if (!name) return "??";
    return name.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase();
}
function firstName(name) { return (name || "").split(" ")[0]; }
function formatDate(dateStr) {
    if (!dateStr) return "—";
    try { return new Date(dateStr).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }); }
    catch { return dateStr; }
}
function formatDateTime(dt) {
    if (!dt) return "—";
    try { return new Date(dt).toLocaleString("en-GB", { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" }); }
    catch { return dt; }
}
function formatTime(timeStr) {
    if (!timeStr) return "—";
    try {
        const [h, m] = timeStr.split(":");
        const d = new Date(); d.setHours(+h, +m);
        return d.toLocaleTimeString("en-GB", { hour: "2-digit", minute: "2-digit" });
    } catch { return timeStr; }
}
function formatDays(days) {
    if (!days || !days.length) return "Flexible";
    return days.map(d => d[0] + d.slice(1).toLowerCase()).join(", ");
}
function starsSVG(count) {
    return Array.from({ length: 5 }, (_, i) => {
        const on = i < count;
        return `<svg width="14" height="14" viewBox="0 0 24 24" fill="${on ? "#97B154" : "none"}" stroke="${on ? "#97B154" : "#d9d9d6"}"><polygon points="12 2 15 9 22 9.3 17 14 18.5 21 12 17.3 5.5 21 7 14 2 9.3 9 9 12 2"/></svg>`;
    }).join("");
}
function fillInput(id, value) { const el = document.getElementById(id); if (el) el.value = value || ""; }
function val(id) { return document.getElementById(id)?.value || ""; }
function showInlineAlert(el, msg, type) { if (!el) return; el.textContent = msg; el.className = `modal-alert ${type} show`; }
function openModal(id) { document.getElementById(id)?.classList.add("open"); document.body.style.overflow = "hidden"; }
function closeModal(id) { document.getElementById(id)?.classList.remove("open"); document.body.style.overflow = ""; }
function showToast(msg, ok = true) {
    let t = document.getElementById("fl-toast");
    if (!t) {
        t = document.createElement("div"); t.id = "fl-toast";
        t.style.cssText = "position:fixed;bottom:24px;right:24px;z-index:9999;padding:12px 20px;border-radius:10px;font-size:14px;font-weight:500;box-shadow:0 4px 24px rgba(0,0,0,0.18);transition:opacity 0.3s;";
        document.body.appendChild(t);
    }
    t.textContent = msg; t.style.background = ok ? "var(--fl-moss-700)" : "#c0392b";
    t.style.color = "#fff"; t.style.opacity = "1";
    clearTimeout(t._timer); t._timer = setTimeout(() => { t.style.opacity = "0"; }, 3000);
}