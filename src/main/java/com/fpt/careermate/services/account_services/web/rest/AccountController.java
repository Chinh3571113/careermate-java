package com.fpt.careermate.services.account_services.web.rest;

import com.fpt.careermate.services.account_services.service.AccountImp;
import com.fpt.careermate.services.email_services.service.EmailImp;
import com.fpt.careermate.services.account_services.service.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.account_services.service.dto.request.AccountUpdateRequest;
import com.fpt.careermate.services.account_services.service.dto.response.AccountResponse;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.common.util.ChangePassword;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Account", description = "Manage account")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AccountController {
    AccountImp accountImp;
    EmailImp emailImp;

    @PostMapping
    @Operation(summary = "Create Account", description = "Create a new account")
    ApiResponse<AccountResponse> createUser(@RequestBody @Valid AccountCreationRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .result(accountImp.createAccount(request))
                .build();
    }
    @GetMapping("/all")
    @Operation(summary = "Get All Accounts", description = "return all accounts with pagination")
    ApiResponse<PageResponse<AccountResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<AccountResponse> result = accountImp.getAccounts(pageable);
        return ApiResponse.<PageResponse<AccountResponse>>builder()
                .code(200)
                .result(result)
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Account by ID", description = "Delete an account by its ID")
    ApiResponse<Void> deleteUser(@PathVariable int id) {
        accountImp.deleteAccount(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Delete account successfully")
                .build();
    }

    @PostMapping("/verify-email/{email}")
    @Operation(summary = "Forget Password", description = "Handle forget password request")
    ApiResponse<String> forgetPassword(@PathVariable String email) {
        String verified = emailImp.verifyEmail(email);
        return ApiResponse.<String>builder()
                .code(200)
                .message("If the email exists, a password reset link has been sent.")
                .result(verified)
                .build();
    }

    @PostMapping("verify-otp")
    @Operation(summary = "Verify Code", description = "Verify the code sent to the user's email")
    ApiResponse<String> verifyCode(@RequestParam String email, @RequestParam Integer code) {
        String token = emailImp.verifyOtp(email, code);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Code verified successfully")
                .result(token)
                .build();
    }

    @PutMapping("/change-password/{email}")
    @Operation(summary = "Change Password", description = "Change the user's password using a valid token")
    ApiResponse<String> changePassword(@RequestBody ChangePassword changePassword, @PathVariable String email
    ) {
        String result = emailImp.changePassword(changePassword, email);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Password changed successfully")
                .result(result)
                .build();
    }

}
