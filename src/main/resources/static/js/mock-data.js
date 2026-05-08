// =============================================================
// mock-data.js — Driving School Scheduling System Mock Data
// =============================================================

const MOCK_DATA = {
  student: {
    id: "STU-2024-0042",
    name: "Alex Rivera",
    email: "alex.rivera@email.com",
    phone: "+1 (555) 234-7890",
    avatar: "AR",
    enrolledDate: "2024-10-01",
    packageType: "Standard (20 Lessons)",
    totalLessons: 20,
    completedLessons: 11,
    cancelledLessons: 2,
    upcomingLessons: 3,
    remainingLessons: 9,
    licenseType: "Class C",
    assignedInstructor: "Marcus Chen"
  },

  instructors: [
    {
      id: "INS-001",
      name: "Marcus Chen",
      avatar: "MC",
      rating: 4.9,
      speciality: "Highway & Freeway",
      availableDays: ["Mon", "Tue", "Wed", "Thu"],
      color: "#FF6044"
    },
    {
      id: "INS-002",
      name: "Sofia Reyes",
      avatar: "SR",
      rating: 4.8,
      speciality: "Urban & Parking",
      availableDays: ["Mon", "Wed", "Fri", "Sat"],
      color: "#4ECDC4"
    },
    {
      id: "INS-003",
      name: "James Okafor",
      avatar: "JO",
      rating: 4.7,
      speciality: "Night Driving",
      availableDays: ["Tue", "Thu", "Fri", "Sat"],
      color: "#A78BFA"
    },
    {
      id: "INS-004",
      name: "Priya Nair",
      avatar: "PN",
      rating: 4.95,
      speciality: "Defensive Driving",
      availableDays: ["Mon", "Tue", "Thu", "Sat"],
      color: "#34D399"
    }
  ],

  availableSlots: [
    // Marcus Chen slots
    { id: "SL-001", instructorId: "INS-001", instructorName: "Marcus Chen", date: "2025-07-14", time: "09:00", duration: 60, status: "available" },
    { id: "SL-002", instructorId: "INS-001", instructorName: "Marcus Chen", date: "2025-07-14", time: "11:00", duration: 60, status: "booked" },
    { id: "SL-003", instructorId: "INS-001", instructorName: "Marcus Chen", date: "2025-07-14", time: "14:00", duration: 60, status: "available" },
    { id: "SL-004", instructorId: "INS-001", instructorName: "Marcus Chen", date: "2025-07-15", time: "09:00", duration: 60, status: "available" },
    { id: "SL-005", instructorId: "INS-001", instructorName: "Marcus Chen", date: "2025-07-15", time: "13:00", duration: 60, status: "booked" },
    { id: "SL-006", instructorId: "INS-001", instructorName: "Marcus Chen", date: "2025-07-16", time: "10:00", duration: 60, status: "available" },
    // Sofia Reyes slots
    { id: "SL-007", instructorId: "INS-002", instructorName: "Sofia Reyes", date: "2025-07-14", time: "10:00", duration: 60, status: "available" },
    { id: "SL-008", instructorId: "INS-002", instructorName: "Sofia Reyes", date: "2025-07-14", time: "13:00", duration: 60, status: "available" },
    { id: "SL-009", instructorId: "INS-002", instructorName: "Sofia Reyes", date: "2025-07-16", time: "09:00", duration: 60, status: "booked" },
    { id: "SL-010", instructorId: "INS-002", instructorName: "Sofia Reyes", date: "2025-07-16", time: "11:00", duration: 60, status: "available" },
    { id: "SL-011", instructorId: "INS-002", instructorName: "Sofia Reyes", date: "2025-07-18", time: "14:00", duration: 60, status: "available" },
    // James Okafor slots
    { id: "SL-012", instructorId: "INS-003", instructorName: "James Okafor", date: "2025-07-15", time: "17:00", duration: 60, status: "available" },
    { id: "SL-013", instructorId: "INS-003", instructorName: "James Okafor", date: "2025-07-15", time: "19:00", duration: 60, status: "available" },
    { id: "SL-014", instructorId: "INS-003", instructorName: "James Okafor", date: "2025-07-17", time: "18:00", duration: 60, status: "booked" },
    { id: "SL-015", instructorId: "INS-003", instructorName: "James Okafor", date: "2025-07-19", time: "17:00", duration: 60, status: "available" },
    // Priya Nair slots
    { id: "SL-016", instructorId: "INS-004", instructorName: "Priya Nair", date: "2025-07-14", time: "08:00", duration: 60, status: "available" },
    { id: "SL-017", instructorId: "INS-004", instructorName: "Priya Nair", date: "2025-07-15", time: "10:00", duration: 60, status: "available" },
    { id: "SL-018", instructorId: "INS-004", instructorName: "Priya Nair", date: "2025-07-17", time: "09:00", duration: 60, status: "booked" },
    { id: "SL-019", instructorId: "INS-004", instructorName: "Priya Nair", date: "2025-07-19", time: "11:00", duration: 60, status: "available" },
    { id: "SL-020", instructorId: "INS-004", instructorName: "Priya Nair", date: "2025-07-19", time: "14:00", duration: 60, status: "available" }
  ],

  upcomingLessons: [
    {
      id: "LES-0039",
      instructorId: "INS-001",
      instructorName: "Marcus Chen",
      instructorAvatar: "MC",
      date: "2025-07-14",
      time: "09:00",
      duration: 60,
      type: "Highway Driving",
      status: "confirmed",
      location: "Meet: 123 Main St Parking Lot",
      notes: "Bring your learner's permit"
    },
    {
      id: "LES-0040",
      instructorId: "INS-002",
      instructorName: "Sofia Reyes",
      instructorAvatar: "SR",
      date: "2025-07-17",
      time: "11:00",
      duration: 60,
      type: "Urban Parking",
      status: "confirmed",
      location: "Meet: Downtown Community Center",
      notes: "Parallel parking focus session"
    },
    {
      id: "LES-0041",
      instructorId: "INS-001",
      instructorName: "Marcus Chen",
      instructorAvatar: "MC",
      date: "2025-07-21",
      time: "10:00",
      duration: 60,
      type: "Freeway Merging",
      status: "pending",
      location: "Meet: 123 Main St Parking Lot",
      notes: ""
    }
  ],

  lessonHistory: [
    { id: "LES-0001", instructorName: "Marcus Chen", date: "2024-10-08", time: "09:00", type: "Introduction & Basics", status: "completed", score: 88 },
    { id: "LES-0002", instructorName: "Marcus Chen", date: "2024-10-15", time: "09:00", type: "Parking Lot Maneuvers", status: "completed", score: 82 },
    { id: "LES-0003", instructorName: "Sofia Reyes", date: "2024-10-22", time: "11:00", type: "Urban Streets", status: "completed", score: 85 },
    { id: "LES-0004", instructorName: "Sofia Reyes", date: "2024-10-29", time: "11:00", type: "Intersections", status: "cancelled", score: null },
    { id: "LES-0005", instructorName: "Marcus Chen", date: "2024-11-05", time: "09:00", type: "Residential Roads", status: "completed", score: 90 },
    { id: "LES-0006", instructorName: "Priya Nair", date: "2024-11-12", time: "10:00", type: "Defensive Driving 1", status: "completed", score: 91 },
    { id: "LES-0007", instructorName: "Priya Nair", date: "2024-11-19", time: "10:00", type: "Defensive Driving 2", status: "completed", score: 87 },
    { id: "LES-0008", instructorName: "Marcus Chen", date: "2024-11-26", time: "09:00", type: "Highway On-Ramp", status: "completed", score: 84 },
    { id: "LES-0009", instructorName: "Marcus Chen", date: "2024-12-03", time: "09:00", type: "Lane Changes", status: "cancelled", score: null },
    { id: "LES-0010", instructorName: "Marcus Chen", date: "2024-12-10", time: "09:00", type: "Highway Driving", status: "completed", score: 89 },
    { id: "LES-0011", instructorName: "James Okafor", date: "2024-12-17", time: "18:00", type: "Night Driving 1", status: "completed", score: 86 },
    { id: "LES-0012", instructorName: "James Okafor", date: "2025-01-07", time: "18:00", type: "Night Driving 2", status: "completed", score: 92 },
    { id: "LES-0013", instructorName: "Sofia Reyes", date: "2025-01-14", time: "11:00", type: "Advanced Parking", status: "completed", score: 94 }
  ],

  milestones: [
    { id: 1, title: "First Lesson Complete", achieved: true, icon: "🚗" },
    { id: 2, title: "Parking Master", achieved: true, icon: "🅿️" },
    { id: 3, title: "Highway Ready", achieved: true, icon: "🛣️" },
    { id: 4, title: "Night Driver", achieved: true, icon: "🌙" },
    { id: 5, title: "10 Lessons Done", achieved: true, icon: "⭐" },
    { id: 6, title: "Defensive Expert", achieved: false, icon: "🛡️" },
    { id: 7, title: "20 Lessons Done", achieved: false, icon: "🏆" },
    { id: 8, title: "Test Ready", achieved: false, icon: "📋" }
  ]
};
