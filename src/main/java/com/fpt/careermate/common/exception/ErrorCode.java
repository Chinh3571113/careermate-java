package com.fpt.careermate.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_JSON(9998, "Invalid JSON format or missing request body", HttpStatus.BAD_REQUEST),
    EXTERNAL_API_ERROR(9997, "Error occurred while calling external API", HttpStatus.SERVICE_UNAVAILABLE),
    RESPONSE_BODY_EMPTY(9996, "Response body from external API is empty", HttpStatus.SERVICE_UNAVAILABLE),
    FILE_NOT_FOUND(9995, "File not found", HttpStatus.INTERNAL_SERVER_ERROR),
    IO_EXCEPTION(9994, "I/O exception occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL(1009, "Your is email existed", HttpStatus.BAD_REQUEST),

    // 20xx: CandidateInvoice
    ORDER_NOT_FOUND(2000, "CandidateInvoice not found", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_ORDER(2001, "Cannot delete CandidateInvoice if status is not PENDING", HttpStatus.FORBIDDEN),
    PAYMENT_FAILED(2002, "Payment processing failed", HttpStatus.PAYMENT_REQUIRED),
    CAN_NOT_PAY_FOR_FREE_PACKAGE(2003, "Cannot create payment URL for FREE package", HttpStatus.BAD_REQUEST),
    HAS_ACTIVE_PACKAGE(2004,
            "You already have an active package. Cannot pay new package until the current package expires.",
            HttpStatus.BAD_REQUEST),

    // 30xx: CandidatePackage
    PACKAGE_NOT_FOUND(3000, "CandidatePackage not found", HttpStatus.NOT_FOUND),
    USING_FREE_PACAKGE(3001, "You are using FREE package", HttpStatus.BAD_REQUEST),

    // 40xx: Recruiter
    INVALID_WEBSITE(4000, "Website is not reachable", HttpStatus.BAD_REQUEST),
    INVALID_LOGO_URL(4001, "Logo URL is not reachable", HttpStatus.BAD_REQUEST),
    RECRUITER_ALREADY_EXISTS(4002, "You have already created a recruiter profile", HttpStatus.BAD_REQUEST),
    RECRUITER_NOT_FOUND(4003, "Recruiter profile not found", HttpStatus.NOT_FOUND),
    RECRUITER_ALREADY_APPROVED(4004, "Recruiter profile is already approved", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(4005, "Role not found in system", HttpStatus.INTERNAL_SERVER_ERROR),
    PENDING_UPDATE_REQUEST_EXISTS(4006,
            "You already have a pending profile update request. Please wait for admin review or cancel the existing request.",
            HttpStatus.BAD_REQUEST),
    UPDATE_REQUEST_NOT_FOUND(4007, "Profile update request not found", HttpStatus.NOT_FOUND),
    UPDATE_REQUEST_ALREADY_PROCESSED(4008, "This update request has already been processed",
            HttpStatus.BAD_REQUEST),
    RECRUITER_NOT_VERIFIED(4009,
            "Your recruiter profile is not yet approved by admin. Please wait for verification.",
            HttpStatus.FORBIDDEN),
    // 40xx: Recruiter
    RECRUITER_ALREADY_REJECTED(4006, "Recruiter profile is already rejected", HttpStatus.BAD_REQUEST),
    ACCOUNT_BANNED(4007, "Your account has been banned. You cannot update your profile.", HttpStatus.FORBIDDEN),
    CANNOT_UPDATE_NON_REJECTED_PROFILE(4008,
            "You can only update organization information if your account is REJECTED. Current status allows you to access the system.",
            HttpStatus.BAD_REQUEST),

    // 50xx: JdSkill
    SKILL_EXISTED(5000, "JdSkill existed", HttpStatus.BAD_REQUEST),

    // 60xx: Job Posting
    JOB_POSTING_NOT_FOUND(6000, "Job posting not found", HttpStatus.NOT_FOUND),
    CANNOT_MODIFY_JOB_POSTING(6001, "Cannot modify job posting if status is DELETED or PAUSED",
            HttpStatus.FORBIDDEN),
    CANNOT_APPLY_TO_JOB_POSTING(6002, "Cannot apply to job posting if status is not OPEN", HttpStatus.FORBIDDEN),
    ALREADY_APPLIED_TO_JOB_POSTING(6003, "You have already applied to this job posting", HttpStatus.BAD_REQUEST),
    JOB_POSTING_EXPIRED(6005, "Job posting is expired", HttpStatus.BAD_REQUEST),
    CANDIDATE_PROFILE_INCOMPLETE(6006, "Your profile is incomplete. Please complete your profile before applying",
            HttpStatus.BAD_REQUEST),
    CANNOT_PAUSE_JOB_POSTING(6007, "Cannot pause job posting if status is not ACTIVE", HttpStatus.FORBIDDEN),
    INVALID_EXPIRATION_DATE(6008, "Expiration date must be in the future", HttpStatus.BAD_REQUEST),
    DUPLICATE_JOB_POSTING_TITLE(6009, "Job posting title already exists", HttpStatus.BAD_REQUEST),
    JOB_POSTING_FORBIDDEN(6010, "You are not allowed to access this job posting", HttpStatus.FORBIDDEN),
    JD_SKILL_NOT_FOUND(6011, "JdSkill not found", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_JOB_POSTING(6012, "Cannot delete job posting if status is ACTIVE or EXPIRED or PAUSED",
            HttpStatus.FORBIDDEN),
    INVALID_STATUS_TRANSITION(6013, "Only PENDING job postings can be approved or rejected",
            HttpStatus.BAD_REQUEST),
    REJECTION_REASON_REQUIRED(6014, "Rejection reason is required when rejecting a job posting",
            HttpStatus.BAD_REQUEST),
    INVALID_APPROVAL_STATUS(6015, "Invalid approval status. Must be APPROVED or REJECTED", HttpStatus.BAD_REQUEST),
    JOB_FEEDBACK_ALREADY_EXISTS(6016, "You have already provided this type of feedback for this job posting",
            HttpStatus.BAD_REQUEST),
    JOB_FEEDBACK_NOT_FOUND(6017, "Job feedback not found", HttpStatus.NOT_FOUND),
    INVALID_FEEDBACK_TYPE(6018, "Invalid feedback type. Must be one of: like, dislike, save, view",
            HttpStatus.BAD_REQUEST),
    CANNOT_CREATE_JOB_POSTING_PROPERTY(6019, "Cannot create job posting property", HttpStatus.BAD_REQUEST),
    CANNOT_CREATE_JOB_POSTING_COLLECTION(6020, "Cannot create job posting collection", HttpStatus.BAD_REQUEST),

    // 70xx: Coach
    LESSON_NOT_FOUND(7000, "Lesson not found", HttpStatus.NOT_FOUND),
    COURSE_NOT_FOUND(7001, "Course not found", HttpStatus.NOT_FOUND),
    ROADMAP_NOT_FOUND(7002, "Roadmap not found", HttpStatus.NOT_FOUND),
    TOPIC_NOT_FOUND(7003, "Topic not found", HttpStatus.NOT_FOUND),
    SUBTOPIC_NOT_FOUND(7004, "Subtopic not found", HttpStatus.NOT_FOUND),
    CANNOT_CREATE_ROADMAP_PROPERTY(7005, "Cannot create roadmap property", HttpStatus.BAD_REQUEST),

    BLOG_NOT_FOUND(1010, "Blog not found", HttpStatus.NOT_FOUND),
    BLOG_INVALID_STATUS(1011, "Invalid blog status", HttpStatus.BAD_REQUEST),
    BLOG_UNAUTHORIZED(1012, "You are not authorized to modify this blog", HttpStatus.FORBIDDEN),
    BLOG_NOT_EXISTED(1013, "Blog does not exist", HttpStatus.NOT_FOUND),
    BLOG_NOT_PUBLISHED(1014, "Blog is not published", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_EXISTED(1015, "Comment does not exist", HttpStatus.NOT_FOUND),
    COMMENT_UNAUTHORIZED(1016, "You are not authorized to modify this comment", HttpStatus.FORBIDDEN),
    RATING_NOT_EXISTED(1017, "Rating does not exist", HttpStatus.NOT_FOUND),
    INVALID_FILE(1018, "Invalid file name or path", HttpStatus.BAD_REQUEST),
    FILE_STORAGE_ERROR(1019, "Could not store file", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ROLE(1020, "Invalid role. Only CANDIDATE or RECRUITER allowed during registration",
            HttpStatus.BAD_REQUEST),
    ROLE_CONFLICT(1021,
            "This email is already registered with a different role. Please use a different email or login with the correct role.",
            HttpStatus.CONFLICT),

    PROFILE_NOT_FOUND(1010, "Profile not exist", HttpStatus.BAD_REQUEST),
    CANDIDATE_NOT_FOUND(1011, "Candidate Not found", HttpStatus.BAD_REQUEST),

    OTP_INVALID(1012, "Otp is invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1013, "Otp is expired", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(1014, "Password not Match", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(1015, "Invalid Mail", HttpStatus.BAD_REQUEST),
    USER_INACTIVE(1016, "Your account is not available", HttpStatus.BAD_REQUEST),

    RESUME_NOT_FOUND(1017, "Resume Not Found", HttpStatus.BAD_REQUEST),
    RESUME_ALREADY_EXISTS(1018, "Resume already exists for this candidate", HttpStatus.BAD_REQUEST),
    OVERLOAD(1019, "You just only 3 elements", HttpStatus.BAD_REQUEST),

    // Resume detail
    EDUCATION_NOT_FOUND(1020, "Your school is not exist", HttpStatus.BAD_REQUEST),
    CERTIFICATE_NOT_FOUND(1021, "Your Certificate is not exist", HttpStatus.BAD_REQUEST),
    FOREIGN_LANGUAGE_NOT_FOUND(1022, "Your Foreign Language not found", HttpStatus.BAD_REQUEST),
    SKILL_NOT_FOUND(1023, "Your Skill not found", HttpStatus.BAD_REQUEST),
    AWARD_NOT_FOUND(1024, "Your Award not found", HttpStatus.BAD_REQUEST),
    WORK_EXPERIENCE_NOT_FOUND(1025, "Work exp not found", HttpStatus.BAD_REQUEST),
    HIGHLIGHT_PROJECT_NOT_FOUND(1027, "Highlight Project not found", HttpStatus.BAD_REQUEST),
    TOKEN_REUSE_DETECTED(1025, "Detected reuse of refresh token (possible attack)", HttpStatus.FORBIDDEN),
    CANDIDATE_PROFILE_ALREADY_EXISTS(1026, "Your Profile is already created", HttpStatus.BAD_REQUEST),
    WORK_MODEL_NOT_FOUND(1027, "Work model not found", HttpStatus.BAD_REQUEST),
    JOB_APPLICATION_NOT_FOUND(1028, "Job not fount", HttpStatus.BAD_REQUEST),

    // 80xx: Notification
    NOTIFICATION_NOT_FOUND(8000, "Notification not found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST(8001, "Invalid request", HttpStatus.BAD_REQUEST),
    RESUME_NOT_ACTIVE(8002,"Your Resume is not active" ,HttpStatus.BAD_REQUEST ),

    CAN_NOT_PAY_FOR_BASIC_PACKAGE(2005, "Cannot create payment URL for BASIC package", HttpStatus.BAD_REQUEST),
    RECRUITER_INVOICE_NOT_FOUND(2006, "RecruiterInvoice not found", HttpStatus.NOT_FOUND),
    CANDIDATE_INVOICE_NOT_FOUND(2007, "CandidateInvoice not found", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_MY_RECRUITER_INVOICE(
            2008, "Cannot delete RecruiterInvoice if status is not ACTIVE",
            HttpStatus.FORBIDDEN),

    // 90xx: Interview
    INTERVIEW_SESSION_NOT_FOUND(9000, "Interview session not found", HttpStatus.NOT_FOUND),
    INTERVIEW_SESSION_ALREADY_COMPLETED(9001, "Interview session is already completed", HttpStatus.BAD_REQUEST),
    INTERVIEW_QUESTION_NOT_FOUND(9002, "Interview question not found", HttpStatus.NOT_FOUND),
    INTERVIEW_SESSION_FORBIDDEN(9003, "You are not allowed to access this interview session", HttpStatus.FORBIDDEN),
    INTERVIEW_QUESTION_ALREADY_ANSWERED(9004, "This question has already been answered", HttpStatus.BAD_REQUEST),
        INTERVIEW_SESSION_NOT_ONGOING(9005, "Interview session is not ongoing", HttpStatus.BAD_REQUEST),

    // 90xx: Job Application
    JOB_APPLY_NOT_FOUND(9000, "Job application not found", HttpStatus.NOT_FOUND),

    // 91xx: Interview Scheduling
    INTERVIEW_NOT_FOUND(9100, "Interview schedule not found", HttpStatus.NOT_FOUND),
    INTERVIEW_ALREADY_SCHEDULED(9101, "Interview already scheduled for this job application", HttpStatus.BAD_REQUEST),
    INVALID_SCHEDULE_DATE(9102, "Scheduled date must be in the future", HttpStatus.BAD_REQUEST),
    INTERVIEW_ALREADY_CONFIRMED(9103, "Interview has already been confirmed by candidate", HttpStatus.BAD_REQUEST),
    CANNOT_RESCHEDULE_COMPLETED_INTERVIEW(9104, "Cannot reschedule a completed or cancelled interview", HttpStatus.BAD_REQUEST),
    RESCHEDULE_TOO_LATE(9105, "Cannot reschedule interview less than 2 hours before scheduled time", HttpStatus.BAD_REQUEST),
    SCHEDULING_CONFLICT(9109, "Scheduling conflict detected - time slot not available", HttpStatus.CONFLICT),
    INTERVIEW_NOT_YET_COMPLETED(9109, "Interview has not yet reached scheduled end time", HttpStatus.BAD_REQUEST),
    CANNOT_MARK_NO_SHOW_BEFORE_TIME(9110, "Cannot mark as no-show before scheduled interview time", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_COMPLETED_INTERVIEW(9111, "Cannot cancel a completed interview", HttpStatus.BAD_REQUEST),
    INVALID_DURATION(9112, "Invalid interview duration", HttpStatus.BAD_REQUEST),
    INTERVIEW_TOO_SHORT(9113, "Interview must last at least 50% of scheduled duration to complete early", HttpStatus.BAD_REQUEST),
    INTERVIEW_CANNOT_BE_MODIFIED(9114, "Cannot modify completed, cancelled, or no-show interviews", HttpStatus.BAD_REQUEST),

    // Company Review Error Codes (9300-9399)
    REVIEW_NOT_FOUND(9300, "Company review not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_REVIEW(9301, "You are not authorized to review this company", HttpStatus.FORBIDDEN),
    REVIEW_ALREADY_SUBMITTED(9302, "You have already submitted a review for this job application", HttpStatus.BAD_REQUEST),
    
    // Employment Verification Error Codes (9400-9499)
    EMPLOYMENT_VERIFICATION_NOT_FOUND(9400, "Employment verification record not found", HttpStatus.NOT_FOUND),
    EMPLOYMENT_VERIFICATION_EXISTS(9401, "Employment verification already exists for this job application", HttpStatus.BAD_REQUEST),
    EMPLOYMENT_ALREADY_TERMINATED(9402, "Employment has already been terminated", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
