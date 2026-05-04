// =============================================================
// script.js — DriveTrack Student Dashboard
// Modular vanilla JS with mock API functions
// =============================================================

'use strict';

// ── State ──────────────────────────────────────────────────────
const STATE = {
  currentSection: 'dashboard',
  selectedInstructor: null,
  selectedDate: null,
  selectedTime: null,
  selectedLessonForCancel: null,
  selectedLessonForReschedule: null,
  historyFilter: 'all',
  slotFilter: 'all',
};

// ── Mock API Layer ─────────────────────────────────────────────
// Simulates async fetch() calls to a real backend

const api = {
  /**
   * GET /api/student/profile
   * Returns student profile + progress data
   */
  getStudentProfile: () =>
    mockFetch({ ...MOCK_DATA.student }),

  /**
   * GET /api/lessons/upcoming
   * Returns student's upcoming lessons
   */
  getUpcomingLessons: () =>
    mockFetch([...MOCK_DATA.upcomingLessons]),

  /**
   * GET /api/lessons/history
   * Returns completed + cancelled lessons
   */
  getLessonHistory: () =>
    mockFetch([...MOCK_DATA.lessonHistory]),

  /**
   * GET /api/slots
   * Returns all available time slots
   */
  getAvailableSlots: () =>
    mockFetch([...MOCK_DATA.availableSlots]),

  /**
   * GET /api/instructors
   */
  getInstructors: () =>
    mockFetch([...MOCK_DATA.instructors]),

  /**
   * POST /api/lessons/book
   * Books a lesson slot
   */
  bookLesson: (payload) => {
    // Validate payload
    if (!payload.instructorId || !payload.date || !payload.time) {
      return mockFetch(null, 400, 'Missing required fields');
    }
    // Simulate booking
    const newLesson = {
      id: `LES-${String(Date.now()).slice(-4)}`,
      instructorId: payload.instructorId,
      instructorName: payload.instructorName,
      instructorAvatar: payload.instructorAvatar,
      date: payload.date,
      time: payload.time,
      duration: 60,
      type: payload.type || 'General Practice',
      status: 'confirmed',
      location: 'Meet: 123 Main St Parking Lot',
      notes: payload.notes || '',
    };
    MOCK_DATA.upcomingLessons.push(newLesson);
    MOCK_DATA.student.upcomingLessons++;
    // Mark slot as booked
    const slot = MOCK_DATA.availableSlots.find(
      s => s.instructorId === payload.instructorId &&
           s.date === payload.date &&
           s.time === payload.time
    );
    if (slot) slot.status = 'booked';
    return mockFetch({ lesson: newLesson }, 200);
  },

  /**
   * POST /api/lessons/:id/cancel
   * Cancels an upcoming lesson
   */
  cancelLesson: (lessonId) => {
    const idx = MOCK_DATA.upcomingLessons.findIndex(l => l.id === lessonId);
    if (idx === -1) return mockFetch(null, 404, 'Lesson not found');
    const [removed] = MOCK_DATA.upcomingLessons.splice(idx, 1);
    removed.status = 'cancelled';
    MOCK_DATA.lessonHistory.unshift({ ...removed, score: null });
    MOCK_DATA.student.cancelledLessons++;
    MOCK_DATA.student.upcomingLessons = Math.max(0, MOCK_DATA.student.upcomingLessons - 1);
    return mockFetch({ lessonId }, 200);
  },

  /**
   * POST /api/lessons/:id/reschedule
   * Reschedules an existing lesson
   */
  rescheduleLesson: (lessonId, newDate, newTime) => {
    const lesson = MOCK_DATA.upcomingLessons.find(l => l.id === lessonId);
    if (!lesson) return mockFetch(null, 404, 'Lesson not found');
    lesson.date = newDate;
    lesson.time = newTime;
    lesson.status = 'pending';
    return mockFetch({ lesson }, 200);
  },

  /**
   * GET /api/progress
   */
  getProgress: () =>
    mockFetch({
      student: MOCK_DATA.student,
      milestones: MOCK_DATA.milestones,
    }),
};

/**
 * Simulates a fetch() with artificial latency
 */
function mockFetch(data, status = 200, message = 'OK') {
  return new Promise((resolve, reject) => {
    const delay = 300 + Math.random() * 400;
    setTimeout(() => {
      if (status >= 400) {
        reject({ status, message });
      } else {
        resolve({ status, data, message });
      }
    }, delay);
  });
}

// ── Navigation ─────────────────────────────────────────────────

/**
 * Navigate to a section by ID
 */
function navigateTo(sectionId) {
  // Update state
  STATE.currentSection = sectionId;

  // Hide all sections
  document.querySelectorAll('.page-section').forEach(s => s.classList.remove('active'));
  // Show target
  const target = document.getElementById(`section-${sectionId}`);
  if (target) target.classList.add('active');

  // Update nav items
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.toggle('active', item.dataset.section === sectionId);
  });

  // Update topbar title
  const titleMap = {
    dashboard:  'Dashboard',
    slots:      'Available Slots',
    book:       'Book a Lesson',
    upcoming:   'Upcoming Lessons',
    reschedule: 'Reschedule Lesson',
    cancel:     'Cancel Lesson',
    history:    'Lesson History',
    progress:   'Progress Tracker',
  };
  document.getElementById('page-title').textContent = titleMap[sectionId] || sectionId;

  // Close mobile sidebar
  closeSidebar();

  // Load section data
  loadSection(sectionId);

  // Scroll top
  document.querySelector('.main-content').scrollTop = 0;
}

/** Load data for a specific section */
function loadSection(sectionId) {
  switch (sectionId) {
    case 'dashboard':   loadDashboard(); break;
    case 'slots':       loadSlots(); break;
    case 'book':        loadBookingForm(); break;
    case 'upcoming':    loadUpcoming(); break;
    case 'reschedule':  loadReschedule(); break;
    case 'cancel':      loadCancelPage(); break;
    case 'history':     loadHistory(); break;
    case 'progress':    loadProgress(); break;
  }
}

// ── Sidebar ────────────────────────────────────────────────────

function openSidebar() {
  document.getElementById('sidebar').classList.add('open');
  document.getElementById('sidebar-overlay').classList.add('show');
}

function closeSidebar() {
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('sidebar-overlay').classList.remove('show');
}

// ── Dashboard ──────────────────────────────────────────────────

async function loadDashboard() {
  try {
    const [profileRes, upcomingRes] = await Promise.all([
      api.getStudentProfile(),
      api.getUpcomingLessons(),
    ]);

    const student = profileRes.data;
    const upcoming = upcomingRes.data;

    // Populate stats
    document.getElementById('dash-completed').textContent = student.completedLessons;
    document.getElementById('dash-upcoming').textContent = student.upcomingLessons;
    document.getElementById('dash-remaining').textContent = student.remainingLessons;
    document.getElementById('dash-cancelled').textContent = student.cancelledLessons;

    // Next lesson
    const nextLessonEl = document.getElementById('dash-next-lesson');
    if (upcoming.length > 0) {
      const next = upcoming[0];
      nextLessonEl.innerHTML = `
        <div class="d-flex align-items-center gap-3 mb-3">
          <div class="avatar">${next.instructorAvatar}</div>
          <div>
            <div style="font-family:var(--font-display);font-weight:700">${next.type}</div>
            <div style="color:var(--text-muted);font-size:12px">with ${next.instructorName}</div>
          </div>
          <span class="badge badge-${next.status} ms-auto">${capitalise(next.status)}</span>
        </div>
        <div class="d-flex gap-3" style="font-size:13px;color:var(--text-dim)">
          <span>📅 ${formatDate(next.date)}</span>
          <span>🕐 ${formatTime(next.time)}</span>
          <span>⏱ 60 min</span>
        </div>
        <div style="margin-top:8px;font-size:12px;color:var(--text-muted)">📍 ${next.location}</div>
      `;
    } else {
      nextLessonEl.innerHTML = `<div class="empty-state" style="padding:20px">
        <div class="empty-icon">📅</div>
        <div class="empty-title">No Upcoming Lessons</div>
        <div class="empty-text">Book a lesson to get started</div>
      </div>`;
    }

    // Progress bar
    const pct = Math.round((student.completedLessons / student.totalLessons) * 100);
    document.getElementById('dash-progress-bar').style.width = pct + '%';
    document.getElementById('dash-progress-pct').textContent = pct + '% Complete';

    // Instructor list
    renderDashInstructors();

  } catch (err) {
    console.error('Dashboard load error:', err);
  }
}

async function renderDashInstructors() {
  const res = await api.getInstructors();
  const instructors = res.data;
  const el = document.getElementById('dash-instructors');

  el.innerHTML = instructors.map(ins => `
    <div class="instructor-card">
      <div class="instructor-top">
        <div class="avatar sm" style="background:${ins.color}22;border-color:${ins.color};color:${ins.color}">${ins.avatar}</div>
        <div>
          <div class="instructor-name">${ins.name}</div>
          <div class="stars">★★★★★</div>
        </div>
        <div class="rating-val ms-auto">${ins.rating}</div>
      </div>
      <div class="instructor-specialty">${ins.speciality}</div>
      <div style="font-size:11px;color:var(--text-muted)">${ins.availableDays.join(' · ')}</div>
    </div>
  `).join('');
}

// ── Available Slots ────────────────────────────────────────────

async function loadSlots() {
  const grid = document.getElementById('slots-grid');
  grid.innerHTML = `<div class="spinner mx-auto" style="grid-column:1/-1"></div>`;

  try {
    const res = await api.getAvailableSlots();
    const slots = res.data;
    renderSlotsGrid(slots);
  } catch (err) {
    grid.innerHTML = `<div style="color:var(--danger);grid-column:1/-1">Failed to load slots.</div>`;
  }
}

function renderSlotsGrid(slots, filter = STATE.slotFilter) {
  const grid = document.getElementById('slots-grid');
  let filtered = slots;
  if (filter !== 'all') {
    filtered = slots.filter(s => s.status === filter);
  }

  if (filtered.length === 0) {
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1">
      <div class="empty-icon">🔍</div>
      <div class="empty-title">No Slots Found</div>
      <div class="empty-text">Try changing the filter</div>
    </div>`;
    return;
  }

  grid.innerHTML = filtered.map(slot => `
    <div class="slot-card ${slot.status}" onclick="${slot.status === 'available' ? `quickBook('${slot.id}')` : ''}">
      <div class="d-flex justify-content-between align-items-start mb-1">
        <div class="slot-time">${formatTime(slot.time)}</div>
        <span class="badge badge-${slot.status}">${capitalise(slot.status)}</span>
      </div>
      <div class="slot-date">${formatDate(slot.date)}</div>
      <div class="slot-instructor">
        <div class="avatar sm">${slot.instructorName.split(' ').map(w=>w[0]).join('')}</div>
        <div class="slot-instructor-name">${slot.instructorName}</div>
      </div>
      ${slot.status === 'available' ? `<button class="btn btn-success btn-sm btn-full mt-2" onclick="quickBook('${slot.id}')">Select Slot</button>` : ''}
    </div>
  `).join('');
}

function filterSlots(filter) {
  STATE.slotFilter = filter;
  document.querySelectorAll('#slots-section .filter-chip').forEach(c => {
    c.classList.toggle('active', c.dataset.filter === filter);
  });
  api.getAvailableSlots().then(res => renderSlotsGrid(res.data, filter));
}

function quickBook(slotId) {
  const slot = MOCK_DATA.availableSlots.find(s => s.id === slotId);
  if (!slot) return;
  // Pre-populate booking form
  STATE.selectedInstructor = slot.instructorId;
  STATE.selectedDate = slot.date;
  STATE.selectedTime = slot.time;
  navigateTo('book');
}

// ── Book Lesson ────────────────────────────────────────────────

async function loadBookingForm() {
  try {
    const res = await api.getInstructors();
    const instructors = res.data;

    const select = document.getElementById('book-instructor');
    select.innerHTML = `<option value="">Choose Instructor…</option>` +
      instructors.map(i => `<option value="${i.id}" data-name="${i.name}" data-avatar="${i.avatar}">${i.name} — ${i.speciality}</option>`).join('');

    // Restore state if coming from quick-book
    if (STATE.selectedInstructor) {
      select.value = STATE.selectedInstructor;
      onInstructorChange();
    }
    if (STATE.selectedDate) {
      document.getElementById('book-date').value = STATE.selectedDate;
      onDateChange();
    }

    updateBookingSummary();
  } catch (err) {
    console.error('Booking form load error:', err);
  }
}

function onInstructorChange() {
  const select = document.getElementById('book-instructor');
  STATE.selectedInstructor = select.value;
  STATE.selectedTime = null;
  updateTimeSlots();
  updateBookingSummary();
}

function onDateChange() {
  STATE.selectedDate = document.getElementById('book-date').value;
  STATE.selectedTime = null;
  updateTimeSlots();
  updateBookingSummary();
}

function updateTimeSlots() {
  const container = document.getElementById('book-time-slots');
  if (!STATE.selectedInstructor || !STATE.selectedDate) {
    container.innerHTML = `<div style="color:var(--text-muted);font-size:13px">Select instructor & date first</div>`;
    return;
  }

  const slots = MOCK_DATA.availableSlots.filter(
    s => s.instructorId === STATE.selectedInstructor && s.date === STATE.selectedDate
  );

  if (slots.length === 0) {
    container.innerHTML = `<div style="color:var(--text-muted);font-size:13px">No slots available on this date</div>`;
    return;
  }

  container.innerHTML = slots.map(slot => `
    <div class="time-chip ${slot.status === 'booked' ? 'taken' : ''} ${STATE.selectedTime === slot.time ? 'selected' : ''}"
         onclick="${slot.status !== 'booked' ? `selectTime('${slot.time}')` : ''}"
    >${formatTime(slot.time)}</div>
  `).join('');
}

function selectTime(time) {
  STATE.selectedTime = time;
  updateTimeSlots();
  updateBookingSummary();
}

function updateBookingSummary() {
  const instrEl = document.getElementById('summary-instructor');
  const dateEl  = document.getElementById('summary-date');
  const timeEl  = document.getElementById('summary-time');
  const durEl   = document.getElementById('summary-duration');

  const instructor = MOCK_DATA.instructors.find(i => i.id === STATE.selectedInstructor);
  instrEl.textContent = instructor ? instructor.name : '—';
  dateEl.textContent  = STATE.selectedDate ? formatDate(STATE.selectedDate) : '—';
  timeEl.textContent  = STATE.selectedTime ? formatTime(STATE.selectedTime) : '—';
  durEl.textContent   = '60 minutes';

  document.getElementById('book-submit-btn').disabled =
    !(STATE.selectedInstructor && STATE.selectedDate && STATE.selectedTime);
}

async function submitBooking() {
  const btn = document.getElementById('book-submit-btn');
  const instructor = MOCK_DATA.instructors.find(i => i.id === STATE.selectedInstructor);
  const notes = document.getElementById('book-notes').value.trim();
  const lessonType = document.getElementById('book-type').value;

  btn.disabled = true;
  btn.innerHTML = `<div class="spinner" style="width:16px;height:16px;margin:0 auto"></div>`;

  try {
    const res = await api.bookLesson({
      instructorId: STATE.selectedInstructor,
      instructorName: instructor.name,
      instructorAvatar: instructor.avatar,
      date: STATE.selectedDate,
      time: STATE.selectedTime,
      type: lessonType,
      notes,
    });

    showToast('success', 'Lesson Booked!', `${formatDate(STATE.selectedDate)} at ${formatTime(STATE.selectedTime)} with ${instructor.name}`);

    // Reset state
    STATE.selectedInstructor = null;
    STATE.selectedDate = null;
    STATE.selectedTime = null;

    // Navigate to upcoming
    setTimeout(() => navigateTo('upcoming'), 800);
  } catch (err) {
    showToast('error', 'Booking Failed', err.message || 'Please try again.');
    btn.disabled = false;
    btn.textContent = 'Confirm Booking';
  }
}

// ── Upcoming Lessons ────────────────────────────────────────────

async function loadUpcoming() {
  const container = document.getElementById('upcoming-lessons');
  container.innerHTML = `<div class="spinner" style="margin:40px auto"></div>`;

  try {
    const res = await api.getUpcomingLessons();
    const lessons = res.data;

    if (lessons.length === 0) {
      container.innerHTML = `<div class="empty-state">
        <div class="empty-icon">📅</div>
        <div class="empty-title">No Upcoming Lessons</div>
        <div class="empty-text">Book your next lesson to get started</div>
        <button class="btn btn-primary mt-3" onclick="navigateTo('book')">Book a Lesson</button>
      </div>`;
      return;
    }

    const accentColors = ['var(--primary)', 'var(--teal)', 'var(--purple)'];

    container.innerHTML = lessons.map((lesson, i) => `
      <div class="lesson-card" style="--card-accent:${accentColors[i % accentColors.length]}">
        <div class="lesson-card-top">
          <div>
            <div class="lesson-type">${lesson.type}</div>
            <div style="font-size:12px;color:var(--text-muted);margin-top:2px">${lesson.id}</div>
          </div>
          <span class="badge badge-${lesson.status}">${capitalise(lesson.status)}</span>
        </div>
        <div class="lesson-datetime">
          <div class="lesson-dt-item"><span class="icon">📅</span>${formatDate(lesson.date)}</div>
          <div class="lesson-dt-item"><span class="icon">🕐</span>${formatTime(lesson.time)}</div>
          <div class="lesson-dt-item"><span class="icon">⏱</span>60 min</div>
        </div>
        <div class="instructor-cell mb-2">
          <div class="avatar sm">${lesson.instructorAvatar}</div>
          <div style="font-size:13px;color:var(--text-dim)">${lesson.instructorName}</div>
        </div>
        <div style="font-size:12px;color:var(--text-muted)">📍 ${lesson.location}</div>
        ${lesson.notes ? `<div style="font-size:12px;color:var(--text-muted);margin-top:4px">📝 ${lesson.notes}</div>` : ''}
        <div class="lesson-card-footer">
          <button class="btn btn-ghost btn-sm" onclick="startReschedule('${lesson.id}')">✏️ Reschedule</button>
          <button class="btn btn-danger btn-sm" onclick="confirmCancel('${lesson.id}', '${lesson.type}', '${lesson.date}')">✕ Cancel</button>
        </div>
      </div>
    `).join('');

  } catch (err) {
    container.innerHTML = `<div style="color:var(--danger)">Failed to load upcoming lessons.</div>`;
  }
}

// ── Cancel Lesson ──────────────────────────────────────────────

async function loadCancelPage() {
  const container = document.getElementById('cancel-lessons-list');
  container.innerHTML = `<div class="spinner" style="margin:40px auto"></div>`;

  try {
    const res = await api.getUpcomingLessons();
    const lessons = res.data;

    if (lessons.length === 0) {
      container.innerHTML = `<div class="empty-state">
        <div class="empty-icon">✅</div>
        <div class="empty-title">No Lessons to Cancel</div>
        <div class="empty-text">You have no upcoming lessons scheduled</div>
      </div>`;
      return;
    }

    container.innerHTML = `
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>Lesson ID</th>
              <th>Type</th>
              <th>Instructor</th>
              <th>Date</th>
              <th>Time</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${lessons.map(l => `
              <tr>
                <td><span class="lesson-id">${l.id}</span></td>
                <td>${l.type}</td>
                <td>
                  <div class="instructor-cell">
                    <div class="avatar sm">${l.instructorAvatar}</div>
                    ${l.instructorName}
                  </div>
                </td>
                <td>${formatDate(l.date)}</td>
                <td>${formatTime(l.time)}</td>
                <td><span class="badge badge-${l.status}">${capitalise(l.status)}</span></td>
                <td>
                  <button class="btn btn-danger btn-sm" onclick="confirmCancel('${l.id}', '${l.type}', '${l.date}')">
                    Cancel
                  </button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>`;
  } catch (err) {
    container.innerHTML = `<div style="color:var(--danger)">Failed to load lessons.</div>`;
  }
}

function confirmCancel(lessonId, lessonType, lessonDate) {
  STATE.selectedLessonForCancel = lessonId;
  document.getElementById('cancel-modal-info').textContent =
    `${lessonType} on ${formatDate(lessonDate)}`;
  openModal('cancel-modal');
}

async function executeCancel() {
  const lessonId = STATE.selectedLessonForCancel;
  closeModal('cancel-modal');

  try {
    await api.cancelLesson(lessonId);
    showToast('success', 'Lesson Cancelled', 'Your lesson has been removed.');
    loadSection(STATE.currentSection);
    if (STATE.currentSection !== 'cancel') loadCancelPage();
  } catch (err) {
    showToast('error', 'Cancellation Failed', err.message || 'Please try again.');
  }
}

// ── Reschedule ─────────────────────────────────────────────────

async function loadReschedule() {
  const lessonSelect = document.getElementById('reschedule-lesson');
  lessonSelect.innerHTML = `<option value="">Loading…</option>`;

  try {
    const res = await api.getUpcomingLessons();
    const lessons = res.data;

    lessonSelect.innerHTML = `<option value="">Select a lesson…</option>` +
      lessons.map(l => `<option value="${l.id}">${l.type} — ${formatDate(l.date)} ${formatTime(l.time)}</option>`).join('');

    if (STATE.selectedLessonForReschedule) {
      lessonSelect.value = STATE.selectedLessonForReschedule;
      STATE.selectedLessonForReschedule = null;
    }
  } catch (err) {
    lessonSelect.innerHTML = `<option value="">Failed to load</option>`;
  }
}

function startReschedule(lessonId) {
  STATE.selectedLessonForReschedule = lessonId;
  navigateTo('reschedule');
}

async function submitReschedule() {
  const lessonId = document.getElementById('reschedule-lesson').value;
  const newDate  = document.getElementById('reschedule-date').value;
  const newTime  = document.getElementById('reschedule-time').value;

  if (!lessonId || !newDate || !newTime) {
    showToast('warning', 'Missing Fields', 'Please fill in all fields.');
    return;
  }

  const btn = document.getElementById('reschedule-btn');
  btn.disabled = true;
  btn.innerHTML = `<div class="spinner" style="width:16px;height:16px;margin:0 auto"></div>`;

  try {
    await api.rescheduleLesson(lessonId, newDate, newTime);
    showToast('success', 'Reschedule Requested', `New date: ${formatDate(newDate)} at ${newTime}`);
    setTimeout(() => navigateTo('upcoming'), 800);
  } catch (err) {
    showToast('error', 'Reschedule Failed', err.message || 'Please try again.');
    btn.disabled = false;
    btn.textContent = 'Send Reschedule Request';
  }
}

// ── Lesson History ──────────────────────────────────────────────

async function loadHistory() {
  const container = document.getElementById('history-table-body');
  container.innerHTML = `<tr><td colspan="7" class="text-center" style="padding:40px"><div class="spinner" style="margin:auto"></div></td></tr>`;

  try {
    const res = await api.getLessonHistory();
    const lessons = res.data;
    renderHistoryTable(lessons, STATE.historyFilter);
  } catch (err) {
    container.innerHTML = `<tr><td colspan="7" style="color:var(--danger);padding:20px">Failed to load history.</td></tr>`;
  }
}

function renderHistoryTable(lessons, filter = 'all') {
  STATE.historyFilter = filter;
  const filtered = filter === 'all' ? lessons : lessons.filter(l => l.status === filter);
  const container = document.getElementById('history-table-body');

  // Update filter chips
  document.querySelectorAll('#history-section .filter-chip').forEach(c => {
    c.classList.toggle('active', c.dataset.filter === filter);
  });

  if (filtered.length === 0) {
    container.innerHTML = `<tr><td colspan="7"><div class="empty-state" style="padding:40px">
      <div class="empty-icon">📋</div>
      <div class="empty-title">No records found</div>
    </div></td></tr>`;
    return;
  }

  container.innerHTML = filtered.map(l => `
    <tr>
      <td><span class="lesson-id">${l.id}</span></td>
      <td>${l.type}</td>
      <td>
        <div class="instructor-cell">
          <div class="avatar sm">${l.instructorName.split(' ').map(w=>w[0]).join('')}</div>
          ${l.instructorName}
        </div>
      </td>
      <td>${formatDate(l.date)}</td>
      <td>${formatTime(l.time)}</td>
      <td><span class="badge badge-${l.status}">${capitalise(l.status)}</span></td>
      <td>${l.score !== null ? `<span class="score-pill">${l.score}%</span>` : '<span style="color:var(--text-muted)">—</span>'}</td>
    </tr>
  `).join('');
}

function filterHistory(filter) {
  api.getLessonHistory().then(res => renderHistoryTable(res.data, filter));
}

// ── Progress ───────────────────────────────────────────────────

async function loadProgress() {
  try {
    const res = await api.getProgress();
    const { student, milestones } = res.data;

    // Ring chart
    const completed = student.completedLessons;
    const total     = student.totalLessons;
    const pct       = Math.round((completed / total) * 100);
    const r         = 66;
    const circ      = 2 * Math.PI * r;
    const dash      = (completed / total) * circ;

    document.getElementById('progress-ring-circle').setAttribute('stroke-dasharray', `${dash} ${circ - dash}`);
    document.getElementById('progress-pct-text').textContent = pct + '%';

    // Stats
    document.getElementById('prog-completed').textContent = completed;
    document.getElementById('prog-remaining').textContent = student.remainingLessons;
    document.getElementById('prog-cancelled').textContent = student.cancelledLessons;
    document.getElementById('prog-upcoming').textContent  = student.upcomingLessons;

    // Instructor
    document.getElementById('prog-instructor').textContent = student.assignedInstructor;

    // Breakdown bars
    const barsEl = document.getElementById('progress-breakdown');
    const items = [
      { label: 'Completed',  value: completed,              total, color: 'green', bg: 'var(--success-dim)', color2: 'var(--success)' },
      { label: 'Upcoming',   value: student.upcomingLessons, total, color: '',     bg: 'var(--primary-dim)', color2: 'var(--primary)' },
      { label: 'Cancelled',  value: student.cancelledLessons, total, color: '',   bg: 'var(--danger-dim)',  color2: 'var(--danger)' },
      { label: 'Remaining',  value: student.remainingLessons, total, color: 'purple', bg: 'var(--purple-dim)', color2: 'var(--purple)' },
    ];

    barsEl.innerHTML = items.map(item => `
      <div style="margin-bottom:14px">
        <div class="d-flex justify-content-between mb-1">
          <span style="font-size:13px;color:var(--text-dim)">${item.label}</span>
          <span style="font-size:13px;font-weight:700;color:var(--text)">${item.value}</span>
        </div>
        <div class="progress-bar-wrap">
          <div class="progress-bar-fill ${item.color}" style="width:${Math.round(item.value/item.total*100)}%;background:${item.color2}"></div>
        </div>
      </div>
    `).join('');

    // Milestones
    const milestonesEl = document.getElementById('milestones-grid');
    milestonesEl.innerHTML = milestones.map(m => `
      <div class="milestone-item ${m.achieved ? 'achieved' : ''}">
        <div class="milestone-emoji">${m.icon}</div>
        <div class="milestone-title">${m.title}</div>
        ${m.achieved ? `<div style="margin-top:6px;font-size:10px;color:var(--primary);font-weight:700">✓ Earned</div>` : ''}
      </div>
    `).join('');

  } catch (err) {
    console.error('Progress load error:', err);
  }
}

// ── Modal ──────────────────────────────────────────────────────

function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('open');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('open');
}

// ── Toast Notifications ────────────────────────────────────────

function showToast(type, title, message) {
  const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };
  const container = document.getElementById('toast-container');

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <div class="toast-icon">${icons[type] || 'ℹ️'}</div>
    <div class="toast-body">
      <div class="toast-title">${title}</div>
      <div class="toast-msg">${message}</div>
    </div>
    <button class="toast-close" onclick="dismissToast(this.parentElement)">✕</button>
  `;

  container.appendChild(toast);

  // Auto-dismiss after 4s
  setTimeout(() => dismissToast(toast), 4000);
}

function dismissToast(toast) {
  if (!toast || !toast.parentElement) return;
  toast.classList.add('removing');
  setTimeout(() => toast.remove(), 280);
}

// ── Utility Helpers ────────────────────────────────────────────

function formatDate(dateStr) {
  if (!dateStr) return '—';
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
}

function formatTime(timeStr) {
  if (!timeStr) return '—';
  const [h, m] = timeStr.split(':').map(Number);
  const ampm = h >= 12 ? 'PM' : 'AM';
  const hh   = h % 12 || 12;
  return `${hh}:${String(m).padStart(2, '0')} ${ampm}`;
}

function capitalise(str) {
  return str ? str.charAt(0).toUpperCase() + str.slice(1) : '';
}

// Set min date for date pickers to today
function setMinDates() {
  const today = new Date().toISOString().split('T')[0];
  ['book-date', 'reschedule-date'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.min = today;
  });
}

// ── Init ───────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  // Nav bindings
  document.querySelectorAll('.nav-item[data-section]').forEach(item => {
    item.addEventListener('click', () => navigateTo(item.dataset.section));
  });

  // Hamburger
  document.getElementById('hamburger-btn')?.addEventListener('click', openSidebar);
  document.getElementById('sidebar-overlay')?.addEventListener('click', closeSidebar);

  // Modal close on backdrop click
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) closeModal(overlay.id);
    });
  });

  // Set min dates
  setMinDates();

  // Load initial section
  loadDashboard();

  // Welcome toast
  setTimeout(() => {
    showToast('info', 'Welcome back, Alex! 👋', 'You have 3 upcoming lessons this week.');
  }, 1000);
});
