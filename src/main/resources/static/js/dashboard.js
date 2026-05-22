/**
 * dashboard.js  (admin missing pieces — merge into admin.js or load separately)
 * Depends on: api.js, admin.js
 *
 * Adds:
 *   loadStudents()      GET /api/students/getAll
 *   loadPayments()      GET /api/payments/all
 *   approvePayment(id)  PUT /api/payments/approve/{id}
 *   rejectPayment(id)   PUT /api/payments/reject/{id}?rejectionReason=...
 *   loadFeedbackAdmin() GET /api/feedback/public (admin view)
 */

/* ─── Hook into admin nav ───────────────────────────────────────────────── */

document.addEventListener("DOMContentLoaded", () => {
    // Load on first paint for visible panes
    loadStudents();
    loadPayments();
    loadFeedbackAdmin();
});

// admin.js already calls loadCourses/loadInstructors on nav click —
// extend it to also call these when those panes are opened
const _origSetupNav = typeof setupAdminNavigation === "function" ? setupAdminNavigation : null;
document.addEventListener("click", e => {
    const pane = e.target.closest("[data-pane]")?.dataset.pane;
    if (pane === "students")  loadStudents();
    if (pane === "payments")  loadPayments();
    if (pane === "feedback")  loadFeedbackAdmin();
});

/* ─── Students pane ─────────────────────────────────────────────────────── */

async function loadStudents() {
    const container = document.querySelector("#pane-students .student-list, #pane-students .card");
    if (!container) return;

    try {
        const data = await apiJson("/students/getAll");
        const students = data.content || [];

        // Clear old dynamic rows
        container.querySelectorAll(".student-dyn-row").forEach(r => r.remove());

        if (!students.length) {
            container.insertAdjacentHTML("beforeend",
                `<p class="muted student-dyn-row" style="padding:16px;">No students found.</p>`);
            return;
        }

        students.forEach((s, idx) => {
            const row = document.createElement("div");
            row.className = "student-row student-dyn-row";
            row.innerHTML = `
                <span class="avatar-circle sm ${toneClass(idx)}">${initials(s.fullName)}</span>
                <div>
                    <h4>${s.fullName || "—"}</h4>
                    <div class="meta">${s.email || "—"} · ${s.nic || "—"}</div>
                </div>
                <div>
                    <div class="eyebrow">Status</div>
                    <span class="badge ${s.status === "ACTIVE" ? "badge--moss" : "badge--neutral"}">
                        <span class="dot"></span>${s.status || "—"}
                    </span>
                </div>
                <div><div class="eyebrow">Student ID</div><b>${s.studentId || "—"}</b></div>`;
            container.appendChild(row);
        });

    } catch (err) {
        showToast("Could not load students", false);
        console.warn(err);
    }
}

/* ─── Payments pane ─────────────────────────────────────────────────────── */

async function loadPayments() {
    const container = document.querySelector("#pane-payments .payment-list, #pane-payments .card");
    if (!container) return;

    try {
        const data = await apiJson("/payments/all");
        const payments = data.content || [];

        container.querySelectorAll(".payment-dyn-row").forEach(r => r.remove());

        if (!payments.length) {
            container.insertAdjacentHTML("beforeend",
                `<p class="muted payment-dyn-row" style="padding:16px;">No payments found.</p>`);
            return;
        }

        payments.forEach(p => {
            const row = document.createElement("div");
            row.className = "payment-dyn-row";
            row.style.cssText = "display:grid;grid-template-columns:1fr auto auto auto;gap:16px;align-items:center;padding:14px 0;border-bottom:1px solid var(--fl-line-soft);";
            row.innerHTML = `
                <div>
                    <b>${p.studentId || "—"}</b>
                    <div class="meta" style="font-family:var(--fl-font-mono);font-size:11px;letter-spacing:0.06em;color:var(--fl-fg-muted);margin-top:2px;">
                        ${p.paymentMethod || "—"} · Ref: ${p.transactionReference || "—"}
                    </div>
                </div>
                <div>
                    <div class="eyebrow">Amount</div>
                    <b style="font-family:var(--fl-font-display);font-size:22px;letter-spacing:-0.01em;">$${p.amount || 0}</b>
                </div>
                <div>
                    <div class="eyebrow">Status</div>
                    ${paymentStatusBadge(p.status)}
                </div>
                <div style="display:flex;gap:8px;">
                    ${p.status === "PENDING" ? `
                        <button class="btn btn--primary btn--sm" onclick="approvePayment('${p.paymentId}')">Approve</button>
                        <button class="btn btn--danger btn--sm" onclick="promptReject('${p.paymentId}')">Reject</button>
                    ` : "—"}
                </div>`;
            container.appendChild(row);
        });

    } catch (err) {
        showToast("Could not load payments", false);
        console.warn(err);
    }
}

async function approvePayment(paymentId) {
    try {
        await apiJson(`/payments/approve/${paymentId}`, { method: "PUT" });
        showToast("Payment approved.");
        loadPayments();
    } catch (err) {
        showToast(err.message || "Could not approve payment.", false);
    }
}

function promptReject(paymentId) {
    const reason = window.prompt("Rejection reason:");
    if (!reason) return;
    rejectPayment(paymentId, reason);
}

async function rejectPayment(paymentId, reason) {
    try {
        await apiJson(`/payments/reject/${paymentId}?rejectionReason=${encodeURIComponent(reason)}`, { method: "PUT" });
        showToast("Payment rejected.");
        loadPayments();
    } catch (err) {
        showToast(err.message || "Could not reject payment.", false);
    }
}

function paymentStatusBadge(status) {
    const map = {
        PENDING:  "badge--warn",
        APPROVED: "badge--moss",
        REJECTED: "badge--error"
    };
    return `<span class="badge ${map[status] || "badge--neutral"}"><span class="dot"></span>${status || "—"}</span>`;
}

/* ─── Feedback admin view ───────────────────────────────────────────────── */

async function loadFeedbackAdmin() {
    const container = document.querySelector("#pane-feedback .feedback-list, #pane-feedback .card");
    if (!container) return;

    try {
        const data = await apiJson("/feedback/public");
        const feedbacks = data.content || [];

        container.querySelectorAll(".feedback-dyn-row").forEach(r => r.remove());

        if (!feedbacks.length) {
            container.insertAdjacentHTML("beforeend",
                `<p class="muted feedback-dyn-row" style="padding:16px;">No feedback yet.</p>`);
            return;
        }

        feedbacks.forEach(f => {
            const row = document.createElement("div");
            row.className = "feedback-dyn-row";
            row.style.cssText = "padding:14px 0;border-bottom:1px solid var(--fl-line-soft);display:grid;grid-template-columns:auto 1fr auto;gap:16px;align-items:start;";
            row.innerHTML = `
                <div class="stars">${starsSVG(f.rating)}</div>
                <div>
                    <b>${f.studentName || "—"}</b>
                    <div class="meta" style="font-family:var(--fl-font-mono);font-size:11px;color:var(--fl-fg-muted);margin-top:2px;">
                        → ${f.instructorName || "—"} · ${formatDate(f.feedbackDate)}
                    </div>
                    <p style="font-size:14px;margin:6px 0 0;color:var(--fl-fg-muted);">"${f.comment || "—"}"</p>
                </div>
                <span class="badge badge--neutral">${f.rating}/5</span>`;
            container.appendChild(row);
        });

    } catch (err) {
        console.warn("Could not load feedback:", err);
    }
}

/* ─── Helpers (shared with admin.js) ───────────────────────────────────── */

function toneClass(i) {
    return ["ink", "tone-b", "tone-c", "tone-d", "tone-e"][i % 5];
}

function initials(name) {
    if (!name) return "??";
    return name.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase();
}

function formatDate(dateStr) {
    if (!dateStr) return "—";
    try { return new Date(dateStr).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }); }
    catch { return dateStr; }
}

function starsSVG(count) {
    return Array.from({ length: 5 }, (_, i) => {
        const on = i < count;
        return `<svg width="14" height="14" viewBox="0 0 24 24"
            fill="${on ? "#97B154" : "none"}" stroke="${on ? "#97B154" : "#d9d9d6"}">
            <polygon points="12 2 15 9 22 9.3 17 14 18.5 21 12 17.3 5.5 21 7 14 2 9.3 9 9 12 2"/>
        </svg>`;
    }).join("");
}
