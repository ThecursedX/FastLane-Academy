/**
 * student.js — FastLane Academy
 * Minimal shim: all core functions are defined in student.html's <script> block.
 * This file only adds helpers that student.html doesn't already define.
 *
 * NOTE: cancelRequest() and rescheduleRequest() are intentionally NOT defined here.
 * They are defined in student.html where they have access to loadSchedule(),
 * loadOverview(), and showRebookBanner(). Defining them here would overwrite
 * those more complete versions.
 */

/* ─── My Requests (lesson queue) — used by schedule pane ─────────────── */

async function loadMyRequests(userId) {
    try {
        const uid = userId || localStorage.getItem('fl-userId');
        const data = await apiJson(`/requests/myRequests?studentId=${uid}`);
        const requests = data.content || [];
        renderMyRequests(requests);
    } catch (err) { console.warn('Could not load requests:', err); }
}

function renderMyRequests(requests) {
    const container = document.getElementById('upcoming-lessons-list');
    if (!container) return;

    if (!requests.length) {
        container.innerHTML = `<p class="muted" style="padding:12px 0;">No lesson requests yet. Pick a slot from the Courses tab.</p>`;
        return;
    }

    // DISABLED requests surface at the top, SLOT_TAKEN second so student can act immediately
    const statusOrder = { DISABLED: 0, SLOT_TAKEN: 1, SELECTED: 2, IN_QUEUE: 3, PENDING: 4, CANCELLED: 5 };
    requests.sort((a, b) => {
        const so = (statusOrder[a.status] ?? 9) - (statusOrder[b.status] ?? 9);
        if (so !== 0) return so;
        return (a.date || '').localeCompare(b.date || '');
    });

    container.innerHTML = requests.map(r => {
        // SELECTED: cancel only allowed >24h before lesson
        const lessonMs  = r.date ? new Date(`${r.date}T${r.time || '00:00'}`).getTime() : 0;
        const hoursLeft = lessonMs ? (lessonMs - Date.now()) / 3600000 : Infinity;

        const canCancel     = (r.status === 'SELECTED' && hoursLeft > 24) || r.status === 'DISABLED' || r.status === 'SLOT_TAKEN';
        const within24h     = r.status === 'SELECTED' && hoursLeft <= 24 && hoursLeft > 0;
        // DISABLED and SLOT_TAKEN requests can also be rescheduled
        const canReschedule = r.status === 'PENDING' || r.status === 'IN_QUEUE' || r.status === 'DISABLED' || r.status === 'SLOT_TAKEN';
        const uid = localStorage.getItem('fl-userId');

        // Banner for DISABLED or SLOT_TAKEN requests
        const disabledBanner = r.status === 'DISABLED'
            ? `<div style="font-size:12px;color:#b45309;background:#fef3c7;padding:6px 10px;border-radius:6px;margin-top:4px;">
                ⚠️ Your instructor has disabled this slot. Please cancel or reschedule your request.
               </div>`
            : r.status === 'SLOT_TAKEN'
            ? `<div style="font-size:12px;color:#1d4ed8;background:#eff6ff;padding:6px 10px;border-radius:6px;margin-top:4px;">
                ℹ️ This slot is already booked by another student. You're next in line if they cancel. You can also reschedule.
               </div>`
            : '';

        return `
        <div style="padding:14px 0;border-bottom:1px solid var(--fl-line-soft);display:flex;flex-direction:column;gap:6px;">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;">
                <div>
                    <b>${r.date || '—'}</b>
                    <span class="muted" style="font-size:12px;margin-left:8px;">Course: ${r.courseId} · Instructor: ${r.instructorId}</span>
                    ${r.lessonId ? `<span class="muted" style="font-size:12px;margin-left:8px;">Lesson: <b>${r.lessonId}</b></span>` : ''}
                </div>
                ${requestStatusBadge(r.status)}
            </div>
            ${disabledBanner}
            <div style="display:flex;gap:6px;align-items:center;flex-wrap:wrap;">
                ${canCancel     ? `<button class="btn btn--ghost btn--sm" style="color:var(--fl-error);" onclick="cancelRequest('${r.requestId}','${uid}')">Cancel</button>` : ''}
                ${within24h     ? `<span style="font-size:11px;color:#b91c1c;">⏰ Within 24h — contact instructor to cancel</span>` : ''}
                ${canReschedule ? `<button class="btn btn--ghost btn--sm" onclick="rescheduleRequest('${r.requestId}','${uid}')">Reschedule</button>` : ''}
            </div>
        </div>`;
    }).join('');
}

function requestStatusBadge(status) {
    const map = {
        PENDING:    'badge--warn',
        IN_QUEUE:   'badge--neutral',
        SLOT_TAKEN: 'badge--neutral',
        SELECTED:   'badge--moss',
        CANCELLED:  'badge--danger',
        DISABLED:   'badge--danger'
    };
    const labels = {
        PENDING:    'Pending',
        IN_QUEUE:   'In queue',
        SLOT_TAKEN: 'Slot taken',
        SELECTED:   'Scheduled',
        CANCELLED:  'Cancelled',
        DISABLED:   'Slot disabled'
    };
    return `<span class="badge ${map[status]||'badge--neutral'}"><span class="dot"></span>${labels[status]||status}</span>`;
}