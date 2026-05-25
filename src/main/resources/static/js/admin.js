/**
 * admin.js — FastLane Academy
 * Depends on: api.js (loaded first)
 *
 * Flow handled here:
 *   Overview    → students, instructors, courses, payments counts + tables
 *   Students    → list, deactivate, add
 *   Payments    → list, approve, reject  (payment approval also grants enrollment access)
 *   Enrollments → list pending enrollments per course, shows paymentStatus column,
 *                 approve / reject buttons
 *   Courses     → list, add, edit, archive, delete archived
 *   Instructors → list, add
 *   Lessons     → process FIFO queue (PENDING+claimed → SCHEDULED)
 *   Feedback    → list, delete
 */

document.addEventListener("DOMContentLoaded", () => {
    if (localStorage.getItem("fl-role") !== "ADMIN") {
        window.location.href = "auth.html";
        return;
    }

    applyTheme(localStorage.getItem("fl-theme") || "light");

    const email = localStorage.getItem("fl-email") || "";
    const name  = email.split("@")[0] || "Admin";
    setText("adminName",   name);
    setText("adminAvatar", name.slice(0, 2).toUpperCase());

    document.querySelectorAll("[data-pane]").forEach(a => {
        a.addEventListener("click", e => {
            e.preventDefault();
            const pane = a.dataset.pane;
            switchPane(pane);
            paneLoaders[pane]?.();
        });
    });

    document.querySelectorAll(".day-btn").forEach(b =>
        b.addEventListener("click", () => b.classList.toggle("selected"))
    );

    document.addEventListener("keydown", e => {
        if (e.altKey && e.key === "d") toggleThemeNav();
    });

    loadOverview();
});

/* ─── Pane routing ────────────────────────────────────────────────────── */

const paneMeta = {
    overview:    { crumbs: "Console · Overview",     title: "FastLane operations" },
    students:    { crumbs: "People · Enrolment",     title: "Students" },
    payments:    { crumbs: "Finance · Transactions", title: "Payments & billing" },
    courses:     { crumbs: "Catalogue · Pricing",    title: "Courses" },
    instructors: { crumbs: "People · Staff",         title: "Instructors" },
    feedback:    { crumbs: "Reviews · Inbox",        title: "Feedback" },
};

const paneLoaders = {
    overview:    loadOverview,
    students:    loadStudents,
    payments:    loadPayments,
    courses:     loadCourses,
    instructors: loadInstructors,
    feedback:    loadFeedback,
};

function switchPane(name) {
    document.querySelectorAll(".dash__nav a[data-pane]").forEach(x =>
        x.classList.toggle("active", x.dataset.pane === name)
    );
    document.querySelectorAll(".pane").forEach(p =>
        p.classList.toggle("active", p.id === "pane-" + name)
    );
    const meta = paneMeta[name];
    if (meta) { setText("crumbs", meta.crumbs); setText("pageTitle", meta.title); }
    window.scrollTo({ top: 0, behavior: "smooth" });
}

/* ─── Overview ────────────────────────────────────────────────────────── */

async function loadOverview() {
    try {
        const [sRes, iRes, cRes, pRes] = await Promise.allSettled([
            apiJson("/students/getAll"),
            apiJson("/instructors/getAllInstructors"),
            apiJson("/courses/getAllCourses"),
            apiJson("/payments/all"),
        ]);

        const students    = sRes.status === "fulfilled" ? (sRes.value.content || sRes.value || []) : [];
        const instructors = iRes.status === "fulfilled" ? (iRes.value.content || iRes.value || []) : [];
        const courses     = cRes.status === "fulfilled" ? (cRes.value.content || cRes.value || []) : [];
        const payments    = pRes.status === "fulfilled" ? (pRes.value.content || pRes.value || []) : [];

        const activeInst = instructors.filter(i => i.status === "ACTIVE").length;
        const activeCrs  = courses.filter(c => c.status === "ACTIVE").length;
        const pendingPay = payments.filter(p => p.status === "PENDING").length;

        setText("ov-headline", `${activeInst} instructors active · ${activeCrs} courses live · ${pendingPay} payments pending`);
        setText("ov-students",      students.length);
        setText("ov-instructors",   instructors.length);
        setText("ov-courses",       courses.length);
        setText("ov-stat-students", students.length);
        setText("ov-stat-instrs",   instructors.length);
        setText("ov-stat-courses",  courses.length);
        setText("ov-stat-payments", payments.length);
        setText("nav-students-pill", students.length);
        setText("nav-instrs-pill",   instructors.length);

        renderOvStudentsTable(students.slice(-5).reverse());
        renderOvPaymentsTable(payments.filter(p => p.status === "PENDING").slice(0, 5));
        loadOverviewEnrollments();

    } catch (err) {
        console.warn("Overview load error", err);
    }
}

function renderOvStudentsTable(students) {
    const wrap = document.getElementById("ov-students-table");
    if (!wrap) return;
    if (!students.length) { wrap.innerHTML = emptyState("No students yet"); return; }
    wrap.innerHTML = `<div class="tbl-wrap" style="border:none;">
    <table class="tbl">
      <thead><tr><th>Student</th><th>Email</th><th>Status</th></tr></thead>
      <tbody>${students.map(s => `
        <tr>
          <td><div class="ux"><span class="avatar-circle" style="width:28px;height:28px;font-size:11px;">${initials(s.fullName)}</span><b>${s.fullName || "—"}</b></div></td>
          <td class="meta">${s.email || "—"}</td>
          <td>${statusBadge(s.status)}</td>
        </tr>`).join("")}
      </tbody>
    </table></div>`;
}

function renderOvPaymentsTable(payments) {
    const wrap = document.getElementById("ov-payments-table");
    if (!wrap) return;
    if (!payments.length) { wrap.innerHTML = emptyState("No pending payments"); return; }
    wrap.innerHTML = `<div class="tbl-wrap" style="border:none;">
    <table class="tbl">
      <thead><tr><th>Student ID</th><th>Amount</th><th>Method</th><th>Status</th><th></th></tr></thead>
      <tbody>${payments.map(p => `
        <tr>
          <td class="mono" style="font-size:12px;">${p.studentId || "—"}</td>
          <td><b>$${(+p.amount || 0).toFixed(2)}</b></td>
          <td class="meta">${p.paymentMethod || "—"}</td>
          <td>${paymentStatusBadge(p.status)}</td>
          <td>
            <div style="display:flex;gap:6px;">
              <button class="btn btn--ghost btn--sm" onclick="viewPayment(${JSON.stringify(p).replace(/"/g,'&quot;')})">View</button>
              ${p.status === "PENDING" ? `
              <button class="btn btn--primary btn--sm" onclick="approvePayment('${p.paymentId}')">Approve</button>
              <button class="btn btn--ghost btn--sm" onclick="promptReject('${p.paymentId}')">Reject</button>` : ""}
            </div>
          </td>
        </tr>`).join("")}
      </tbody>
    </table></div>`;
}

/* ─── Overview enrollment approvals card ──────────────────────────────── */

async function loadOverviewEnrollments() {
    const wrap = document.getElementById("ov-enroll-wrap");
    const pill = document.getElementById("ov-enroll-pill");
    if (!wrap) return;
    try {
        const cData = await apiJson("/courses/getAllCourses");
        const courses = cData.content || cData || [];
        const allEnrollments = [];
        await Promise.all(courses.map(async c => {
            try {
                const d = await apiJson(`/enrollments/course/${c.courseId}`);
                (d.content || d || []).forEach(e => {
                    e._courseName = c.courseTitle;
                    allEnrollments.push(e);
                });
            } catch (_) {}
        }));
        const pending = allEnrollments.filter(e => e.status !== "APPROVED" && e.status !== "CANCELLED");

        if (pill) {
            if (pending.length) {
                pill.textContent = `${pending.length} pending`;
                pill.style.display = "";
            } else {
                pill.style.display = "none";
            }
        }

        if (!pending.length) {
            wrap.innerHTML = `<p style="padding:16px 20px;color:var(--fl-fg-muted);font-size:14px;margin:0;">All enrollments are approved — nothing to action.</p>`;
            return;
        }

        wrap.innerHTML = `<div class="tbl-wrap" style="border:none;">
    <table class="tbl">
      <thead><tr>
        <th>Enrollment ID</th><th>Student ID</th><th>Course</th>
        <th>Enrolled</th><th>Status</th><th>Payment</th><th>Actions</th>
      </tr></thead>
      <tbody>${pending.map(e => {
            const paymentPaid = e.paymentStatus === "APPROVED" || e.paymentStatus === "PAID";
            return `
        <tr>
          <td class="mono" style="font-size:12px;">${e.enrollmentId || "—"}</td>
          <td class="mono" style="font-size:12px;">${e.studentId || "—"}</td>
          <td><b>${e._courseName || e.courseId || "—"}</b></td>
          <td class="meta">${e.enrolledDate || "—"}</td>
          <td>${enrollmentStatusBadge(e.status)}</td>
          <td>${paymentStatusBadge(e.paymentStatus)}</td>
          <td>
            <div style="display:flex;gap:6px;align-items:center;">
              ${paymentPaid
                ? `<button class="btn btn--primary btn--sm" onclick="approveEnrollmentOv('${e.enrollmentId}')">Accept</button>`
                : `<span style="font-size:12px;color:var(--fl-fg-muted);font-style:italic;">Awaiting payment</span>`}
              <button class="btn btn--ghost btn--sm" onclick="rejectEnrollmentOvPrompt('${e.enrollmentId}')">Reject</button>
            </div>
          </td>
        </tr>`;
        }).join("")}
      </tbody>
    </table></div>`;
    } catch (err) {
        if (wrap) wrap.innerHTML = emptyState("Could not load enrollments");
    }
}

async function approveEnrollmentOv(enrollmentId) {
    try {
        await apiJson(`/enrollments/approve/${enrollmentId}`, { method: "PUT" });
        showToast("Enrollment accepted — student can now book lesson slots.");
        loadOverviewEnrollments(); loadOverview();
    } catch (err) { showToast(err.message || "Failed", false); }
}

function rejectEnrollmentOvPrompt(enrollmentId) {
    const reason = window.prompt("Rejection reason:");
    if (!reason) return;
    apiJson(`/enrollments/reject/${enrollmentId}?reason=${encodeURIComponent(reason)}`, { method: "PUT" })
        .then(() => { showToast("Enrollment rejected."); loadOverviewEnrollments(); loadOverview(); })
        .catch(err => showToast(err.message || "Failed", false));
}

/* ─── Students ────────────────────────────────────────────────────────── */

async function loadStudents() {
    const wrap = document.getElementById("students-table-wrap");
    if (!wrap) return;
    wrap.innerHTML = loadingState("students");
    try {
        const data = await apiJson("/students/getAll");
        const students = data.content || data || [];

        const active = students.filter(s => s.status === "ACTIVE").length;
        setText("st-total",    students.length);
        setText("st-active",   active);
        setText("st-inactive", students.length - active);
        setText("nav-students-pill", students.length);

        if (!students.length) { wrap.innerHTML = emptyState("No students yet"); return; }

        wrap.innerHTML = `<table class="tbl">
      <thead><tr><th>Student</th><th>ID</th><th>NIC</th><th>Contact</th><th>Status</th><th></th></tr></thead>
      <tbody>${students.map(s => `
        <tr>
          <td><div class="ux"><span class="avatar-circle" style="width:32px;height:32px;font-size:12px;">${initials(s.fullName)}</span>
            <div><b>${s.fullName || "—"}</b><div class="meta">${s.email || "—"}</div></div></div></td>
          <td class="mono" style="font-size:12px;">${s.studentId || "—"}</td>
          <td class="meta">${s.nic || "—"}</td>
          <td>${s.contactNumber || "—"}</td>
          <td>${statusBadge(s.status)}</td>
          <td style="display:flex;gap:4px;align-items:center;">
            ${s.status !== "INACTIVE" ? `<button class="icon-btn" title="Deactivate" onclick="deactivateStudent('${s.studentId}')">✕</button>` : ""}
            ${s.status === "INACTIVE" ? `<button class="icon-btn" title="Delete permanently" style="color:#e53935;" onclick="deleteStudent('${s.studentId}', '${(s.fullName||"").replace(/'/g,"\\'")}')">🗑</button>` : ""}
          </td>
        </tr>`).join("")}
      </tbody></table>`;
    } catch (err) {
        wrap.innerHTML = emptyState("Could not load students — " + (err.message || "API error"));
    }
}

async function deactivateStudent(id) {
    if (!confirm(`Deactivate student ${id}?`)) return;
    try {
        await apiJson(`/students/deactivate/${id}`, { method: "PUT" });
        showToast("Student deactivated.");
        loadStudents();
    } catch (err) { showToast(err.message || "Failed", false); }
}

async function deleteStudent(id, name) {
    if (!confirm(`Permanently delete deactivated student "${name || id}"?\n\nThis cannot be undone.`)) return;
    try {
        await apiJson(`/students/delete/${id}`, { method: "DELETE" });
        showToast("Student deleted permanently.");
        loadStudents();
        loadOverview();
    } catch (err) { showToast(err.message || "Failed to delete student", false); }
}

/* ─── Payments ────────────────────────────────────────────────────────── */

async function loadPayments() {
    const wrap = document.getElementById("payments-table-wrap");
    if (!wrap) return;
    wrap.innerHTML = loadingState("payments");
    loadPendingEnrollments();
    try {
        const data = await apiJson("/payments/all");
        const payments = data.content || data || [];

        setText("pay-total",    payments.length);
        setText("pay-pending",  payments.filter(p => p.status === "PENDING").length);
        setText("pay-approved", payments.filter(p => p.status === "APPROVED").length);
        setText("pay-rejected", payments.filter(p => p.status === "REJECTED").length);

        if (!payments.length) { wrap.innerHTML = emptyState("No payments yet"); return; }

        wrap.innerHTML = `<table class="tbl">
      <thead><tr>
        <th>Payment ID</th><th>Student ID</th><th>Enrollment ID</th>
        <th>Amount</th><th>Method</th><th>Ref</th><th>Submitted</th><th>Status</th><th>Actions</th>
      </tr></thead>
      <tbody>${payments.map(p => `
        <tr>
          <td class="mono" style="font-size:11px;">${p.paymentId || "—"}</td>
          <td class="mono" style="font-size:12px;">${p.studentId || "—"}</td>
          <td class="mono" style="font-size:11px;">${p.enrollmentId || "—"}</td>
          <td><b>$${(+p.amount || 0).toFixed(2)}</b></td>
          <td class="meta">${p.paymentMethod || "—"}</td>
          <td class="meta" style="font-size:11px;max-width:120px;overflow:hidden;text-overflow:ellipsis;">${p.transactionReference || "—"}</td>
          <td class="meta">${formatDate(p.submittedAt)}</td>
          <td>${paymentStatusBadge(p.status)}</td>
          <td>
            <div style="display:flex;gap:6px;">
              <button class="btn btn--ghost btn--sm" onclick="viewPayment(${JSON.stringify(p).replace(/"/g,'&quot;')})">View</button>
              ${p.status === "PENDING" ? `
              <button class="btn btn--primary btn--sm" onclick="approvePayment('${p.paymentId}')">Approve</button>
              <button class="btn btn--ghost btn--sm" onclick="promptReject('${p.paymentId}')">Reject</button>` : ""}
            </div>
          </td>
        </tr>`).join("")}
      </tbody></table>`;
    } catch (err) {
        wrap.innerHTML = emptyState("Could not load payments — " + (err.message || "API error"));
    }
}

async function approvePayment(id) {
    try {
        await apiJson(`/payments/approve/${id}`, { method: "PUT" });
        showToast("Payment approved — course access granted.");
        loadPayments(); loadOverview();
    } catch (err) { showToast(err.message || "Failed", false); }
}

function promptReject(id) {
    const reason = window.prompt("Rejection reason:");
    if (!reason) return;
    apiJson(`/payments/reject/${id}?rejectionReason=${encodeURIComponent(reason)}`, { method: "PUT" })
        .then(() => { showToast("Payment rejected."); loadPayments(); loadOverview(); })
        .catch(err => showToast(err.message || "Failed", false));
}

/* ─── View payment detail modal ───────────────────────────────────────── */

function viewPayment(p) {
    const receiptHtml = p.receiptUrl
        ? (() => {
            const src = `/uploads/${p.receiptUrl.replace(/^.*[\\/]/, "")}`;
            const lower = p.receiptUrl.toLowerCase();
            const isImage = /\.(jpe?g|png|gif|webp|bmp)$/.test(lower);
            if (isImage) {
                return `<a href="${src}" target="_blank">
                    <img src="${src}" alt="Payment receipt"
                         style="max-width:100%;max-height:320px;border-radius:8px;border:1px solid var(--fl-line);display:block;cursor:zoom-in;"
                         onerror="this.style.display='none';this.nextElementSibling.style.display=''">
                    <span style="display:none;color:var(--fl-error);font-size:13px;">Could not load image</span>
                </a>`;
            }
            return `<a href="${src}" target="_blank" style="color:var(--fl-accent);text-decoration:underline;font-size:13px;">View receipt →</a>`;
        })()
        : `<span style="color:var(--fl-fg-muted);font-size:13px;">No receipt uploaded</span>`;

    const statusColors = { PENDING: "var(--fl-warn)", APPROVED: "var(--fl-success)", REJECTED: "var(--fl-error)" };
    const statusColor  = statusColors[p.status] || "var(--fl-fg-muted)";

    document.getElementById("vp-modal-body").innerHTML = `
      <div style="display:flex;flex-direction:column;gap:16px;">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <div>
            <div style="font-size:11px;text-transform:uppercase;letter-spacing:.08em;color:var(--fl-fg-muted);margin-bottom:2px;">Payment ID</div>
            <div class="mono" style="font-size:13px;">${p.paymentId || "—"}</div>
          </div>
          <span style="padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;background:${statusColor}22;color:${statusColor};">${p.status || "—"}</span>
        </div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
          <div><div class="vp-lbl">Student ID</div><div class="mono" style="font-size:13px;">${p.studentId || "—"}</div></div>
          <div><div class="vp-lbl">Enrollment ID</div><div class="mono" style="font-size:13px;">${p.enrollmentId || "—"}</div></div>
          <div><div class="vp-lbl">Course ID</div><div class="mono" style="font-size:13px;">${p.courseId || "—"}</div></div>
          <div><div class="vp-lbl">Amount</div><div style="font-size:18px;font-weight:700;">$${(+p.amount || 0).toFixed(2)}</div></div>
          <div><div class="vp-lbl">Payment method</div><div style="font-size:13px;">${p.paymentMethod || "—"}</div></div>
          <div><div class="vp-lbl">Submitted</div><div style="font-size:13px;">${p.submittedAt ? p.submittedAt.split("T")[0] : "—"}</div></div>
        </div>
        <div><div class="vp-lbl">Transaction reference</div><div class="mono" style="font-size:13px;word-break:break-all;">${p.transactionReference || "—"}</div></div>
        ${p.rejectionReason ? `<div style="padding:10px 14px;border-radius:8px;background:color-mix(in srgb,var(--fl-error) 12%,var(--fl-surface,var(--fl-paper)));border:1px solid color-mix(in srgb,var(--fl-error) 40%,transparent);font-size:13px;color:var(--fl-fg);"><b style="color:var(--fl-error);">Rejection reason:</b> ${p.rejectionReason}</div>` : ""}
        <div><div class="vp-lbl">Receipt</div>${receiptHtml}</div>
      </div>`;

    // Show approve/reject in modal footer only if PENDING
    const footer = document.getElementById("vp-modal-footer-actions");
    if (footer) {
        footer.innerHTML = p.status === "PENDING" ? `
          <button class="btn btn--primary" onclick="approvePayment('${p.paymentId}');closeModal('modal-view-payment')">Approve payment</button>
          <button class="btn btn--ghost" onclick="promptReject('${p.paymentId}');closeModal('modal-view-payment')">Reject</button>` : "";
    }

    openModal("modal-view-payment");
}

/* ─── Pending enrollments (with payment status) ───────────────────────── */

async function loadPendingEnrollments() {
    const wrap  = document.getElementById("enroll-pending-wrap");
    const label = document.getElementById("enroll-pending-label");
    if (!wrap) return;

    try {
        const cData   = await apiJson("/courses/getAllCourses");
        const courses  = cData.content || cData || [];

        const allEnrollments = [];
        await Promise.all(courses.map(async c => {
            try {
                const d = await apiJson(`/enrollments/course/${c.courseId}`);
                (d.content || d || []).forEach(e => {
                    e._courseName = c.courseTitle;
                    allEnrollments.push(e);
                });
            } catch (_) {}
        }));

        // Show all non-approved enrollments so admin can act
        const pending = allEnrollments.filter(e => e.status !== "APPROVED" && e.status !== "CANCELLED");
        if (label) label.textContent = `${pending.length} pending`;

        if (!pending.length) {
            wrap.innerHTML = `<p style="padding:16px;color:var(--fl-fg-muted);font-size:14px;margin:0;">All enrollments are approved.</p>`;
            return;
        }

        wrap.innerHTML = `<table class="tbl">
      <thead><tr>
        <th>Enrollment ID</th><th>Student ID</th><th>Course</th>
        <th>Enrolled</th><th>Enrollment</th><th>Payment</th><th>Actions</th>
      </tr></thead>
      <tbody>${pending.map(e => {
            const paymentPaid = e.paymentStatus === "APPROVED" || e.paymentStatus === "PAID";
            return `
        <tr>
          <td class="mono" style="font-size:12px;">${e.enrollmentId || "—"}</td>
          <td class="mono" style="font-size:12px;">${e.studentId || "—"}</td>
          <td><b>${e._courseName || e.courseId || "—"}</b></td>
          <td class="meta">${e.enrolledDate || "—"}</td>
          <td>${enrollmentStatusBadge(e.status)}</td>
          <td>${paymentStatusBadge(e.paymentStatus)}</td>
          <td>
            <div style="display:flex;gap:6px;align-items:center;">
              ${paymentPaid
                ? `<button class="btn btn--primary btn--sm" onclick="approveEnrollment('${e.enrollmentId}')">Approve</button>`
                : `<span style="font-size:12px;color:var(--fl-fg-muted);font-style:italic;">Awaiting payment</span>`}
              <button class="btn btn--ghost btn--sm" onclick="rejectEnrollmentPrompt('${e.enrollmentId}')">Reject</button>
            </div>
          </td>
        </tr>`;
        }).join("")}
      </tbody></table>`;
    } catch (err) {
        if (wrap) wrap.innerHTML = emptyState("Could not load enrollments");
    }
}

async function approveEnrollment(enrollmentId) {
    try {
        await apiJson(`/enrollments/approve/${enrollmentId}`, { method: "PUT" });
        showToast("Enrollment approved — student can now book lesson slots.");
        loadPendingEnrollments(); loadOverviewEnrollments(); loadOverview();
    } catch (err) { showToast(err.message || "Failed", false); }
}

function rejectEnrollmentPrompt(enrollmentId) {
    const reason = window.prompt("Rejection reason:");
    if (!reason) return;
    apiJson(`/enrollments/reject/${enrollmentId}?reason=${encodeURIComponent(reason)}`, { method: "PUT" })
        .then(() => { showToast("Enrollment rejected."); loadPendingEnrollments(); loadOverviewEnrollments(); })
        .catch(err => showToast(err.message || "Failed", false));
}

/* ─── Lesson queue ────────────────────────────────────────────────────── */

async function processLessonQueue() {
    const btn = document.getElementById("process-queue-btn");
    if (btn) { btn.disabled = true; btn.textContent = "Processing…"; }
    try {
        const data = await apiJson("/lessons/processNextLesson", { method: "POST" });
        showToast(data.message || "Lesson scheduled.");
    } catch (err) {
        showToast(err.message || "Nothing in queue or error.", false);
    } finally {
        if (btn) { btn.disabled = false; btn.textContent = "Process next lesson"; }
    }
}

/* ─── Courses ─────────────────────────────────────────────────────────── */

async function loadCourses() {
    const list = document.getElementById("courses-list");
    if (!list) return;
    list.innerHTML = `<div class="empty-state" style="padding:40px;">${spinner()} Loading…</div>`;
    try {
        const data    = await apiJson("/courses/getAllCourses");
        const courses = data.content || data || [];
        setText("courses-count", `${courses.length} course${courses.length !== 1 ? "s" : ""}`);

        if (!courses.length) { list.innerHTML = emptyState("No courses yet — create one above"); return; }

        list.innerHTML = courses.map(c => `
      <div class="course-row" data-course-id="${c.courseId}">
        <div class="glyph ${c.status === "ACTIVE" ? "feat" : ""}">${(c.courseTitle || "?")[0].toUpperCase()}</div>
        <div>
          <h4>${c.courseTitle || "—"}</h4>
          <div class="meta">${c.durationHours || 0}h · ${c.difficultyLevel || "—"}</div>
        </div>
        <div><div class="eyebrow">Difficulty</div>${difficultyBadge(c.difficultyLevel)}</div>
        <div><div class="eyebrow">Price</div><b>${c.price != null ? `$${parseFloat(c.price).toFixed(2)}` : "—"}</b></div>
        <div><div class="eyebrow">Status</div>${statusBadge(c.status)}</div>
        <div><div class="eyebrow">ID</div><b class="mono" style="font-size:12px;">${c.courseId}</b></div>
        <div style="display:flex;gap:6px;">
          <button class="btn btn--ghost btn--sm" onclick="openCourseDetail('${c.courseId}', ${JSON.stringify(c.courseTitle || '').replace(/"/g,'&quot;')})">Details</button>
          <button class="btn btn--ghost btn--sm" onclick="openEditCourse(${JSON.stringify(c).replace(/"/g, "&quot;")})">Edit</button>
          ${c.status === "ACTIVE"
            ? `<button class="btn btn--ghost btn--sm" onclick="deleteCourse('${c.courseId}')">Archive</button>`
            : `<button class="btn btn--ghost btn--sm" style="color:#e53935;" onclick="deleteArchivedCourse('${c.courseId}', ${JSON.stringify(c.courseTitle || '').replace(/"/g,'&quot;')}, this)">Delete archived</button>`}
        </div>
      </div>`).join("");
    } catch (err) {
        list.innerHTML = emptyState("Could not load courses");
    }
}

async function deleteCourse(id) {
    if (!confirm("Archive this course?")) return;
    try {
        await apiJson(`/courses/deleteCourse/${id}`, { method: "PUT" });
        showToast("Course archived.");
        loadCourses();
    } catch (err) { showToast(err.message || "Failed", false); }
}


async function deleteArchivedCourse(id, title, btn) {
    if (!confirm(`Permanently delete archived course "${title || id}"?

This cannot be undone.`)) return;
    try {
        await apiJson(`/courses/deleteArchivedCourse/${id}`, { method: "DELETE" });
        showToast("Archived course deleted permanently.");

        // Remove only the deleted course row from the current page immediately.
        const row = btn?.closest(".course-row");
        if (row) row.remove();

        // Update visible course count without re-rendering the whole page.
        const remaining = document.querySelectorAll("#courses-list .course-row").length;
        setText("courses-count", `${remaining} course${remaining !== 1 ? "s" : ""}`);
        if (remaining === 0) {
            const list = document.getElementById("courses-list");
            if (list) list.innerHTML = emptyState("No courses yet — create one above");
        }

        loadOverview();
    } catch (err) {
        showToast(err.message || "Failed to delete archived course", false);
    }
}

/* ─── Course detail: slots + enrolled students ────────────────────────── */

let _activeCourseId = null;

function openCourseDetail(courseId, courseTitle) {
    _activeCourseId = courseId;
    setText("cd-title", courseTitle || courseId);
    const panel = document.getElementById("course-detail-panel");
    if (panel) { panel.style.display = ""; panel.scrollIntoView({ behavior: "smooth", block: "start" }); }
    switchCourseTab("cd-slots");
    loadCourseSlots(courseId);
}

function closeCourseDetail() {
    const panel = document.getElementById("course-detail-panel");
    if (panel) panel.style.display = "none";
    _activeCourseId = null;
}

function switchCourseTab(tabId) {
    document.querySelectorAll(".course-tab").forEach(b => b.classList.toggle("active", b.dataset.tab === tabId));
    document.querySelectorAll(".course-tab-pane").forEach(p => p.classList.toggle("active", p.id === tabId));
    if (tabId === "cd-slots" && _activeCourseId) loadCourseSlots(_activeCourseId);
    if (tabId === "cd-enrolled" && _activeCourseId) loadCourseEnrolled(_activeCourseId);
}

async function loadCourseSlots(courseId) {
    const wrap = document.getElementById("cd-slots-wrap");
    if (!wrap) return;
    wrap.innerHTML = loadingState("slots");
    try {
        const data  = await apiJson(`/slots/available/${courseId}`);
        const slots = data.content || data || [];
        if (!slots.length) { wrap.innerHTML = emptyState("No available time slots for this course."); return; }

        // Group by day
        const byDay = {};
        slots.forEach(s => {
            const day = s.dayOfWeek || "—";
            if (!byDay[day]) byDay[day] = [];
            byDay[day].push(s);
        });

        const dayOrder = ["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"];
        const sortedDays = Object.keys(byDay).sort((a, b) => dayOrder.indexOf(a) - dayOrder.indexOf(b));

        wrap.innerHTML = `<div style="padding:12px 20px;">
          ${sortedDays.map(day => `
            <div style="margin-bottom:16px;">
              <div style="font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.06em;color:var(--fl-fg-muted);margin-bottom:8px;">${day.charAt(0)+day.slice(1).toLowerCase()}</div>
              <div style="display:flex;flex-wrap:wrap;gap:8px;">
                ${byDay[day].map(s => `
                  <div style="display:flex;align-items:center;gap:6px;padding:6px 12px;border-radius:8px;background:var(--fl-bg-sub,#f5f5f5);border:1px solid var(--fl-border);font-size:13px;">
                    <span style="font-weight:600;">${formatTime(s.time)}</span>
                    <span class="meta" style="font-size:11px;">${s.instructorId || "—"}</span>
                    <span class="badge ${s.enabled ? "badge--moss" : "badge--neutral"}" style="font-size:10px;">${s.enabled ? "Active" : "Disabled"}</span>
                  </div>`).join("")}
              </div>
            </div>`).join("")}
        </div>`;
    } catch (err) {
        wrap.innerHTML = emptyState("Could not load slots — " + (err.message || "API error"));
    }
}

async function loadCourseEnrolled(courseId) {
    const wrap = document.getElementById("cd-enrolled-wrap");
    if (!wrap) return;
    wrap.innerHTML = loadingState("students");
    try {
        const data        = await apiJson(`/enrollments/course/${courseId}`);
        const enrollments = data.content || data || [];
        if (!enrollments.length) { wrap.innerHTML = emptyState("No students enrolled in this course."); return; }

        wrap.innerHTML = `<table class="tbl">
          <thead><tr>
            <th>Enrollment ID</th><th>Student ID</th><th>Enrolled Date</th>
            <th>Enrollment</th><th>Payment</th><th>Actions</th>
          </tr></thead>
          <tbody>${enrollments.map(e => `
            <tr>
              <td class="mono" style="font-size:12px;">${e.enrollmentId || "—"}</td>
              <td class="mono" style="font-size:12px;">${e.studentId || "—"}</td>
              <td class="meta">${e.enrolledDate || "—"}</td>
              <td>${enrollmentStatusBadge(e.status)}</td>
              <td>${paymentStatusBadge(e.paymentStatus)}</td>
              <td>
                ${e.status === "PENDING" || e.status === "REJECTED" ? `
                  <div style="display:flex;gap:6px;">
                    <button class="btn btn--primary btn--sm" onclick="approveEnrollmentFromCourse('${e.enrollmentId}')">Accept</button>
                    ${e.status !== "REJECTED" ? `<button class="btn btn--ghost btn--sm" onclick="rejectEnrollmentFromCourse('${e.enrollmentId}')">Reject</button>` : ""}
                  </div>` : "—"}
              </td>
            </tr>`).join("")}
          </tbody>
        </table>`;
    } catch (err) {
        wrap.innerHTML = emptyState("Could not load enrolled students — " + (err.message || "API error"));
    }
}

async function approveEnrollmentFromCourse(enrollmentId) {
    try {
        await apiJson(`/enrollments/approve/${enrollmentId}`, { method: "PUT" });
        showToast("Enrollment accepted.");
        if (_activeCourseId) loadCourseEnrolled(_activeCourseId);
        loadOverviewEnrollments();
    } catch (err) { showToast(err.message || "Failed", false); }
}

function rejectEnrollmentFromCourse(enrollmentId) {
    const reason = window.prompt("Rejection reason:");
    if (!reason) return;
    apiJson(`/enrollments/reject/${enrollmentId}?reason=${encodeURIComponent(reason)}`, { method: "PUT" })
        .then(() => { showToast("Enrollment rejected."); if (_activeCourseId) loadCourseEnrolled(_activeCourseId); loadOverviewEnrollments(); })
        .catch(err => showToast(err.message || "Failed", false));
}

function formatTime(timeStr) {
    if (!timeStr) return "—";
    try {
        const [h, m] = String(timeStr).split(":");
        const hour = parseInt(h);
        const ampm = hour >= 12 ? "PM" : "AM";
        const h12  = hour % 12 || 12;
        return `${h12}:${m || "00"} ${ampm}`;
    } catch { return String(timeStr); }
}

function openEditCourse(c) {
    setVal("ec-id",    c.courseId);
    setVal("ec-title", c.courseTitle);
    setVal("ec-desc",  c.description);
    setVal("ec-hours", c.durationHours);
    setVal("ec-price", c.price != null ? c.price : "");
    const diffEl = document.getElementById("ec-difficulty");
    if (diffEl) diffEl.value = c.difficultyLevel || "";
    openModal("modal-edit-course");
}

async function submitEditCourse() {
    const btn = document.getElementById("edit-course-btn");
    const alertEl = document.getElementById("edit-course-alert");
    const id    = gv("ec-id");
    const title = gv("ec-title");
    const desc  = gv("ec-desc");
    const hours = parseInt(gv("ec-hours") || "0");
    const diff  = gv("ec-difficulty");
    const price = parseFloat(gv("ec-price") || "0") || null;

    clearAlert(alertEl);
    if (!title || !diff) return showAlert(alertEl, "Title and difficulty are required.", "error");

    btn.disabled = true; btn.textContent = "Saving…";
    try {
        await apiJson(`/courses/updateCourse/${id}`, {
            method: "PUT",
            body: JSON.stringify({ courseTitle: title, description: desc, durationHours: hours, difficultyLevel: diff, price })
        });
        showAlert(alertEl, "Course updated.", "success");
        loadCourses();
        setTimeout(() => closeModal("modal-edit-course"), 1500);
    } catch (err) {
        showAlert(alertEl, err.message || "Failed", "error");
    } finally { btn.disabled = false; btn.textContent = "Save changes"; }
}

/* ─── Instructors ─────────────────────────────────────────────────────── */

async function loadInstructors() {
    const grid = document.getElementById("instructors-grid");
    if (!grid) return;
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;padding:60px;">${spinner()} Loading…</div>`;
    try {
        const data = await apiJson("/instructors/getAllInstructors");
        const instructors = data.content || data || [];

        const active = instructors.filter(i => i.status === "ACTIVE").length;
        const avgExp = instructors.length
            ? Math.round(instructors.reduce((s, i) => s + (+i.experienceYears || 0), 0) / instructors.length) : 0;
        setText("inst-total",    instructors.length);
        setText("inst-active",   active);
        setText("inst-inactive", instructors.length - active);
        setText("inst-avg-exp",  avgExp);
        setText("nav-instrs-pill", instructors.length);

        if (!instructors.length) {
            grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;">${emptyState("No instructors yet")}</div>`;
            return;
        }

        grid.innerHTML = instructors.map((inst, idx) => `
      <div class="inst-card-admin">
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
          <div><b style="font-size:13px;">${inst.licenseId || "—"}</b><span>Licence</span></div>
          <div><b>${(inst.workingDays || []).length}</b><span>Days/wk</span></div>
        </div>
        <div style="font-size:12px;color:var(--fl-fg-muted);">${inst.contactNumber || ""}</div>
        <div style="display:flex;justify-content:flex-end;margin-top:10px;">
          <button class="btn btn--ghost btn--sm" style="color:#e53935;" onclick="deleteInstructor('${inst.instructorId}', ${JSON.stringify(inst.instructorName || '').replace(/"/g,'&quot;')}, this)">Delete</button>
        </div>
      </div>`).join("");
    } catch (err) {
        grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;">${emptyState("Could not load instructors")}</div>`;
    }
}

async function deleteInstructor(id, name, btn) {
    if (!confirm(`Permanently delete instructor "${name || id}"?

This cannot be undone.`)) return;
    try {
        await apiJson(`/instructors/deleteInstructor/${id}`, { method: "DELETE" });
        showToast("Instructor deleted permanently.");

        // Remove only the deleted instructor card from the current page immediately.
        const card = btn?.closest(".inst-card-admin");
        if (card) card.remove();

        const cards = [...document.querySelectorAll("#instructors-grid .inst-card-admin")];
        setText("inst-total", cards.length);
        setText("nav-instrs-pill", cards.length);
        if (cards.length === 0) {
            const grid = document.getElementById("instructors-grid");
            if (grid) grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;">${emptyState("No instructors yet")}</div>`;
        }

        loadOverview();
    } catch (err) {
        showToast(err.message || "Failed to delete instructor", false);
    }
}

/* ─── Feedback ────────────────────────────────────────────────────────── */

async function loadFeedback() {
    const list  = document.getElementById("feedback-list");
    const label = document.getElementById("fb-count-label");
    if (!list) return;
    list.innerHTML = loadingState("feedback");
    try {
        const data  = await apiJson("/feedback/getAll");
        const items = data.content || data || [];

        if (label) label.textContent = `${items.length} review${items.length !== 1 ? "s" : ""}`;
        const ratings = items.map(f => +(f.rating || 0)).filter(r => r > 0);
        const avg     = ratings.length ? (ratings.reduce((a, b) => a + b, 0) / ratings.length).toFixed(1) : "—";
        setText("fb-total", items.length);
        setText("fb-avg",   avg);
        setText("fb-5star", ratings.filter(r => r === 5).length);
        setText("fb-1star", ratings.filter(r => r === 1).length);

        if (!items.length) { list.innerHTML = emptyState("No feedback yet"); return; }

        list.innerHTML = `<div class="tbl-wrap" style="border:none;">
      <table class="tbl">
        <thead><tr><th>Student ID</th><th>Course</th><th>Rating</th><th>Comment</th><th>Date</th><th></th></tr></thead>
        <tbody>${items.map(f => `
          <tr>
            <td class="mono" style="font-size:12px;">${f.studentId || "—"}</td>
            <td class="meta">${f.courseId || "—"}</td>
            <td><b>${f.rating || "—"}</b>/5</td>
            <td style="font-family:var(--fl-font-serif);font-size:14px;max-width:340px;">${f.comment || "—"}</td>
            <td class="meta">${f.feedbackDate || "—"}</td>
            <td><button class="btn btn--ghost btn--sm" onclick="deleteFeedback('${f.feedbackId}')">Delete</button></td>
          </tr>`).join("")}
        </tbody>
      </table></div>`;
    } catch (err) {
        list.innerHTML = emptyState("Could not load feedback");
        if (label) label.textContent = "Error";
    }
}

async function deleteFeedback(id) {
    if (!confirm("Delete this review?")) return;
    try {
        await apiJson(`/feedback/delete/${id}`, { method: "DELETE" });
        showToast("Review deleted.");
        loadFeedback();
    } catch (err) { showToast(err.message || "Failed", false); }
}

/* ─── Modal: Add Student ──────────────────────────────────────────────── */

async function submitAddStudent() {
    const btn     = document.getElementById("add-student-btn");
    const alertEl = document.getElementById("add-student-alert");
    const fullName = gv("as-fullname"), email = gv("as-email"), password = gv("as-password"),
        confirm  = gv("as-confirm"), nic = gv("as-nic"), contactNumber = gv("as-contact"),
        address  = gv("as-address"), dateOfBirth = gv("as-dob") || null,
        emergencyContact = gv("as-emergency");

    clearAlert(alertEl);
    if (!fullName || !email || !password || !nic || !contactNumber || !dateOfBirth)
        return showAlert(alertEl, "Please fill in all required fields.", "error");
    if (password !== confirm)  return showAlert(alertEl, "Passwords do not match.", "error");
    if (password.length < 6)   return showAlert(alertEl, "Password must be at least 6 characters.", "error");

    btn.disabled = true; btn.textContent = "Adding…";
    try {
        await apiJson("/auth/register/student", {
            method: "POST",
            body: JSON.stringify({ fullName, email, password, nic, contactNumber, address, dateOfBirth, emergencyContact, role: "STUDENT" })
        });
        showAlert(alertEl, `${fullName} added successfully.`, "success");
        clearInputs(["as-fullname","as-email","as-password","as-confirm","as-nic","as-contact","as-address","as-dob","as-emergency"]);
        loadStudents(); loadOverview();
        setTimeout(() => closeModal("modal-add-student"), 1600);
    } catch (err) { showAlert(alertEl, err.message || "Failed.", "error"); }
    finally { btn.disabled = false; btn.textContent = "Add student"; }
}

/* ─── Modal: Add Instructor ───────────────────────────────────────────── */

async function submitAddInstructor() {
    const btn     = document.getElementById("add-instructor-btn");
    const alertEl = document.getElementById("add-instructor-alert");
    const fullName = gv("ai-fullname"), email = gv("ai-email"), password = gv("ai-password"),
        confirm  = gv("ai-confirm"), licenseId = gv("ai-license"), contactNumber = gv("ai-contact"),
        vehicleType = gv("ai-vehicle"), experienceYears = parseInt(gv("ai-exp") || "0");
    const workingDays = [...document.querySelectorAll(".day-btn.selected")].map(b => b.dataset.day);

    clearAlert(alertEl);
    if (!fullName || !email || !password || !licenseId || !contactNumber || !vehicleType)
        return showAlert(alertEl, "Please fill in all required fields.", "error");
    if (password !== confirm) return showAlert(alertEl, "Passwords do not match.", "error");

    btn.disabled = true; btn.textContent = "Adding…";
    try {
        await apiJson("/auth/register/instructor", {
            method: "POST",
            body: JSON.stringify({ fullName, email, password, licenseId, contactNumber, vehicleType, experienceYears, workingDays, role: "INSTRUCTOR" })
        });
        showAlert(alertEl, `${fullName} added successfully.`, "success");
        clearInputs(["ai-fullname","ai-email","ai-password","ai-confirm","ai-license","ai-contact","ai-vehicle","ai-exp"]);
        document.querySelectorAll(".day-btn.selected").forEach(b => b.classList.remove("selected"));
        loadInstructors(); loadOverview();
        setTimeout(() => closeModal("modal-add-instructor"), 1600);
    } catch (err) { showAlert(alertEl, err.message || "Failed.", "error"); }
    finally { btn.disabled = false; btn.textContent = "Add instructor"; }
}

/* ─── Modal: New Course ───────────────────────────────────────────────── */

async function submitNewCourse() {
    const btn     = document.getElementById("new-course-btn");
    const alertEl = document.getElementById("new-course-alert");
    const courseTitle = gv("nc-title"), description = gv("nc-desc"),
        durationHours = parseInt(gv("nc-hours") || "0"),
        difficultyLevel = gv("nc-difficulty"),
        price = parseFloat(gv("nc-price") || "0"),
        syllabus = gv("nc-syllabus"), contentStructure = gv("nc-structure");

    clearAlert(alertEl);
    if (!courseTitle || !description || !durationHours || !difficultyLevel || !price)
        return showAlert(alertEl, "Please fill in all required fields.", "error");

    btn.disabled = true; btn.textContent = "Creating…";
    try {
        await apiJson("/courses/addCourse", {
            method: "POST",
            body: JSON.stringify({ courseTitle, description, durationHours, difficultyLevel, price, syllabus, contentStructure })
        });
        showAlert(alertEl, `"${courseTitle}" created.`, "success");
        clearInputs(["nc-title","nc-desc","nc-hours","nc-syllabus","nc-structure","nc-price"]);
        document.getElementById("nc-difficulty").value = "";
        loadCourses(); loadOverview();
        setTimeout(() => closeModal("modal-new-course"), 1600);
    } catch (err) { showAlert(alertEl, err.message || "Failed.", "error"); }
    finally { btn.disabled = false; btn.textContent = "Create course"; }
}

/* ─── Modal: Manual Payment Entry ────────────────────────────────────── */

async function submitManualEntry() {
    const btn     = document.getElementById("manual-entry-btn");
    const alertEl = document.getElementById("manual-entry-alert");
    const studentId = gv("me-student"), courseId = gv("me-course"),
        enrollmentId = gv("me-enrollment"), amount = parseFloat(gv("me-amount") || "0"),
        paymentMethod = gv("me-method"),
        transactionReference = gv("me-ref") || `MANUAL-${Date.now()}`;

    clearAlert(alertEl);
    if (!studentId || !enrollmentId || !amount || !paymentMethod)
        return showAlert(alertEl, "Please fill in all required fields.", "error");

    btn.disabled = true; btn.textContent = "Recording…";
    try {
        const fd = new FormData();
        fd.append("enrollmentId",         enrollmentId);
        fd.append("studentId",            studentId);
        fd.append("courseId",             courseId);
        fd.append("amount",               amount);
        fd.append("paymentMethod",        paymentMethod);
        fd.append("transactionReference", transactionReference);
        fd.append("receiptFile", new Blob([], { type: "application/octet-stream" }), "manual.txt");

        const res  = await fetch("http://localhost:8080/api/payments/submit", {
            method: "POST", credentials: "include", body: fd
        });
        const data = await res.json();
        if (data.code && data.code !== "00")
            throw new Error(data.message || "Backend error");

        showAlert(alertEl, `Payment of $${amount.toFixed(2)} recorded.`, "success");
        clearInputs(["me-student","me-course","me-enrollment","me-amount","me-ref"]);
        document.getElementById("me-method").value = "";
        loadPayments(); loadOverview();
        setTimeout(() => closeModal("modal-manual-entry"), 1600);
    } catch (err) { showAlert(alertEl, err.message || "Failed.", "error"); }
    finally { btn.disabled = false; btn.textContent = "Record payment"; }
}

/* ─── Logout ──────────────────────────────────────────────────────────── */

async function adminLogout() {
    try { await apiJson("/auth/logout", { method: "POST" }); } catch (_) {}
    ["fl-userId","fl-role","fl-email"].forEach(k => localStorage.removeItem(k));
    window.location.href = "auth.html";
}

/* ─── Theme ───────────────────────────────────────────────────────────── */

function applyTheme(t) {
    document.documentElement.dataset.theme = t;
    const moon = document.getElementById("dashMoon");
    const sun  = document.getElementById("dashSun");
    if (moon) moon.style.display = t === "dark" ? "none"  : "block";
    if (sun)  sun.style.display  = t === "dark" ? "block" : "none";
}

function toggleThemeNav() {
    const next = document.documentElement.dataset.theme === "dark" ? "light" : "dark";
    localStorage.setItem("fl-theme", next);
    applyTheme(next);
}

/* ─── Modal helpers ───────────────────────────────────────────────────── */

function openModal(id) { document.getElementById(id)?.classList.add("open"); document.body.style.overflow = "hidden"; }
function closeModal(id) {
    document.getElementById(id)?.classList.remove("open");
    document.body.style.overflow = "";
    document.querySelectorAll(`#${id} .modal-alert`).forEach(el => { el.className = "modal-alert"; el.textContent = ""; });
}
function backdropClose(e, id) { if (e.target === e.currentTarget) closeModal(id); }
document.addEventListener("keydown", e => {
    if (e.key === "Escape") document.querySelectorAll(".modal-backdrop.open").forEach(m => closeModal(m.id));
});
function showAlert(el, msg, type) { if (!el) return; el.textContent = msg; el.className = `modal-alert ${type} show`; }
function clearAlert(el) { if (!el) return; el.textContent = ""; el.className = "modal-alert"; }

/* ─── Badge helpers ───────────────────────────────────────────────────── */

function statusBadge(status) {
    return status === "ACTIVE"
        ? `<span class="badge badge--moss"><span class="dot"></span>Active</span>`
        : `<span class="badge badge--neutral"><span class="dot"></span>${status || "—"}</span>`;
}

function paymentStatusBadge(status) {
    if (!status) return `<span class="badge badge--neutral">Not paid</span>`;
    const map = { PENDING: "badge--warn", APPROVED: "badge--moss", REJECTED: "badge--danger" };
    return `<span class="badge ${map[status] || "badge--neutral"}"><span class="dot"></span>${status}</span>`;
}

function enrollmentStatusBadge(status) {
    const map = { APPROVED: "badge--moss", PENDING: "badge--warn", REJECTED: "badge--danger", CANCELLED: "badge--neutral" };
    return `<span class="badge ${map[status] || "badge--neutral"}"><span class="dot"></span>${status || "—"}</span>`;
}

function difficultyBadge(level) {
    const map = { BEGINNER: "badge--moss", INTERMEDIATE: "badge--warn", ADVANCED: "badge--danger" };
    return `<span class="badge ${map[level] || "badge--neutral"}">${level || "—"}</span>`;
}

/* ─── Utility ─────────────────────────────────────────────────────────── */

function initials(name) {
    if (!name) return "??";
    return name.split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase();
}
function toneClass(i) { return ["ink","tone-b","tone-c","tone-d","tone-e",""][i % 6]; }
function setText(id, v) { const el = document.getElementById(id); if (el) el.textContent = v; }
function setVal(id, v)  { const el = document.getElementById(id); if (el) el.value = v || ""; }
function gv(id)         { return (document.getElementById(id)?.value || "").trim(); }
function clearInputs(ids) { ids.forEach(id => { const el = document.getElementById(id); if (el) el.value = ""; }); }

function formatDate(dateStr) {
    if (!dateStr) return "—";
    try { return new Date(dateStr).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }); }
    catch { return String(dateStr); }
}

function emptyState(msg) {
    return `<div class="empty-state"><p>${msg}</p></div>`;
}
function loadingState(label) {
    return `<div class="empty-state" style="padding:60px 24px;">${spinner()}<p>Loading ${label}…</p></div>`;
}
function spinner() {
    return `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" style="animation:spin 1s linear infinite;"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>`;
}

function showToast(msg, ok = true) {
    let t = document.getElementById("fl-toast");
    if (!t) {
        t = document.createElement("div");
        t.id = "fl-toast";
        t.style.cssText = "position:fixed;bottom:24px;right:24px;z-index:9999;padding:12px 20px;border-radius:10px;font-size:14px;font-weight:500;box-shadow:0 4px 24px rgba(0,0,0,0.18);transition:opacity 0.3s;";
        document.body.appendChild(t);
    }
    t.textContent      = msg;
    t.style.background = ok ? "var(--fl-moss-700)" : "#c0392b";
    t.style.color      = "#fff";
    t.style.opacity    = "1";
    clearTimeout(t._timer);
    t._timer = setTimeout(() => { t.style.opacity = "0"; }, 3500);
}