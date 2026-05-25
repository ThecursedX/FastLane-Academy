package com.example.FastLane.Academy.util;

public class VarList {

    // ── General ───────────────────────────────────────────────────────────
    public static final String RSP_SUCCESS             = "00";
    public static final String RSP_NO_DATA_FOUND       = "01";
    public static final String RSP_NOT_AUTHORISED      = "02";
    public static final String RSP_TOKEN_EXPIRED       = "03";
    public static final String RSP_TOKEN_INVALID       = "04";
    public static final String RSP_ERROR               = "05";
    public static final String RSP_DUPLICATED          = "06";
    public static final String UPDATED_SUCCESSFULLY    = "09";
    public static final String RSP_FAIL                = "10";
    public static final String UNAUTHORIZED            = "75";

    // ── Lesson scheduling ─────────────────────────────────────────────────
    public static final String LESSON_CONFLICT                 = "11";
    public static final String LESSON_SCHEDULED_SUCCESSFULLY   = "00";
    public static final String LESSON_NOT_FOUND                = "13";
    public static final String INSTRUCTOR_CONFLICT             = "20";
    public static final String STUDENT_CONFLICT                = "21";
    public static final String INSTRUCTOR_NOT_FOUND            = "22";
    public static final String INSTRUCTOR_UNAVAILABLE_DAY      = "23";
    public static final String REQUEST_ADDED                   = "15";
    public static final String NO_PENDING_REQUESTS             = "16";
    public static final String CANCELLATION_PERIOD_EXPIRED     = "17";
    public static final String CANNOT_CANCEL                   = "18";
    public static final String INVALID_DAY                     = "30";
    public static final String DATE_OUT_OF_RANGE               = "31";
    public static final String INVALID_TIME_SLOT               = "32";
    public static final String RESCHEDULE_NOT_ALLOWED          = "33";
    public static final String CANCELLATION_NOT_ALLOWED        = "34";
    public static final String INVALID_STATUS_CHANGE           = "35";
    public static final String STATUS_ALREADY_FINAL            = "36";

    // ── Instructor slot management ────────────────────────────────────────
    public static final String SLOT_NOT_FOUND          = "80";
    public static final String SLOT_ALREADY_EXISTS     = "81";
    public static final String SLOT_INVALID_DAY        = "82";
    public static final String SLOT_HAS_ACTIVE_REQUESTS = "83";
    public static final String SLOT_DISABLED             = "87";

    // ── Lesson request (student flow) ─────────────────────────────────────
    public static final String REQUEST_NOT_FOUND        = "84";
    public static final String REQUEST_NOT_RESCHEDULABLE = "85";
    public static final String REQUEST_NOT_CANCELLABLE  = "86";

    // ── Auth / registration ───────────────────────────────────────────────
    public static final String DUPLICATE_EMAIL         = "40";
    public static final String DUPLICATE_LICENSE       = "41";
    public static final String INSTRUCTOR_HAS_FUTURE_LESSONS = "42";
    public static final String INSTRUCTOR_DELETED            = "43";
    public static final String INSTRUCTOR_HAS_ACTIVE_DATA    = "44";
    public static final String DUPLICATE_COURSE        = "50";
    public static final String COURSE_HAS_ENROLLMENTS  = "51";
    public static final String COURSE_NOT_ARCHIVED     = "52";
    public static final String COURSE_DELETED          = "53";
    public static final String ALREADY_ENROLLED        = "60";

    // ── Payment ───────────────────────────────────────────────────────────
    public static final String DUPLICATE_TRANSACTION        = "70";
    public static final String INVALID_PAYMENT_AMOUNT       = "71";
    public static final String INVALID_TRANSACTION_REFERENCE = "72";
    public static final String INVALID_PAYMENT_STATUS       = "73";
    public static final String PAYMENT_UPDATE_NOT_ALLOWED   = "74";

    // ── Student ───────────────────────────────────────────────────────────
    public static final String DUPLICATE_NIC           = "S01";
    public static final String INVALID_AGE             = "S03";
    public static final String STUDENT_NOT_FOUND       = "S04";
}