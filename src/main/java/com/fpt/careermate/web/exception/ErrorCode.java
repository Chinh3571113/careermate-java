package com.fpt.careermate.web.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_JSON(9998, "Invalid JSON format or missing request body", HttpStatus.BAD_REQUEST),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL(1009, "Your is email existed", HttpStatus.BAD_REQUEST),


    //  20xx: Order
    ORDER_NOT_FOUND(2000, "Order not found", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_ORDER(2001, "Cannot delete Order if status is not PENDING", HttpStatus.FORBIDDEN),

    //  30xx: Package
    PACKAGE_NOT_FOUND(3000, "Package not found", HttpStatus.NOT_FOUND),

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
    INVALID_ROLE(1020, "Invalid role. Only CANDIDATE or RECRUITER allowed during registration", HttpStatus.BAD_REQUEST),

    PROFILE_NOT_FOUND(1010,"Profile not exist" ,HttpStatus.BAD_REQUEST ),
    CANDIDATE_NOT_FOUND(1011,"Candidate Not found" ,HttpStatus.BAD_REQUEST ),

    OTP_INVALID(1012,"Otp is invalid" ,HttpStatus.BAD_REQUEST ),
    OTP_EXPIRED(1013, "Otp is expired",HttpStatus.BAD_REQUEST ),
    PASSWORD_NOT_MATCH(1014,"Password not Match" ,HttpStatus.BAD_REQUEST ),
    INVALID_EMAIL_FORMAT(1015,"Invalid Mail" ,HttpStatus.BAD_REQUEST ),
    USER_INACTIVE(1016,"Your account is not available" , HttpStatus.BAD_REQUEST ),

    RESUME_NOT_FOUND(1017,"Resume Not Found" ,HttpStatus.BAD_REQUEST ),
    RESUME_ALREADY_EXISTS(1018,"Resume already exists for this candidate" ,HttpStatus.BAD_REQUEST ),
    OVERLOAD(1019,"You just only 3 elements" , HttpStatus.BAD_REQUEST ),

    //Resume detail
    EDUCATION_NOT_FOUND(1020,"Your school is not exist" ,HttpStatus.BAD_REQUEST ),
    CERTIFICATE_NOT_FOUND(1021,"Your Certificate is not exist" ,HttpStatus.BAD_REQUEST ),
    FOREIGN_LANGUAGE_NOT_FOUND(1022,"Your Foreign Language not found" ,HttpStatus.BAD_REQUEST ),
    SKILL_NOT_FOUND(1023,"Your Skill not found" ,HttpStatus.BAD_REQUEST ),
    AWARD_NOT_FOUND(1024,"Your Award not found" ,HttpStatus.BAD_REQUEST ),
    WORK_EXPERIENCE_NOT_FOUND(1025,"Work exp not found" ,HttpStatus.BAD_REQUEST ),
    TOKEN_REUSE_DETECTED(1025,"Detected reuse of refresh token (possible attack)" ,HttpStatus.FORBIDDEN );



    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
