package com.fpt.careermate.services.file_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.storage.FirebaseStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "File Management", description = "Endpoints for managing file uploads and deletions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileController {

    FirebaseStorageService firebaseStorageService;

    // ADMIN ONLY - Image Upload to Firebase Storage
    @PostMapping("/upload/image")
    @Operation(summary = "Upload Image", description = "Upload an image file to Firebase Storage")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> uploadImage(@RequestParam("image") MultipartFile file) {
        log.info("Starting image upload to Firebase Storage: {}", file.getOriginalFilename());

        // Validate file is not empty
        if (file.isEmpty()) {
            log.warn("Upload rejected: Empty file");
            return ApiResponse.<Map<String, Object>>builder()
                    .code(1004)
                    .message("File cannot be empty")
                    .build();
        }

        // Validate file type
        if (!isImageFile(file)) {
            log.warn("Upload rejected: Invalid file type for {}", file.getOriginalFilename());
            return ApiResponse.<Map<String, Object>>builder()
                    .code(1004)
                    .message("Only image files are allowed")
                    .build();
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            log.warn("Upload rejected: File too large - {} bytes", file.getSize());
            return ApiResponse.<Map<String, Object>>builder()
                    .code(1004)
                    .message("File size cannot exceed 10MB")
                    .build();
        }

        try {
            log.info("Uploading file {} ({} bytes) to Firebase Storage...",
                    file.getOriginalFilename(), file.getSize());

            // Upload to Firebase Storage - this is now synchronous and blocking
            Map<String, Object> uploadResult = firebaseStorageService.uploadFile(file, "careermate/blogs");

            // Verify upload was successful
            if (uploadResult == null || uploadResult.get("secure_url") == null) {
                throw new IOException("Upload completed but no URL returned");
            }

            // Prepare response (same format as before)
            Map<String, Object> result = new HashMap<>();
            result.put("imageUrl", uploadResult.get("secure_url"));
            result.put("publicId", uploadResult.get("public_id"));
            result.put("fileSize", file.getSize());
            result.put("originalName", file.getOriginalFilename());
            result.put("width", uploadResult.get("width"));
            result.put("height", uploadResult.get("height"));

            log.info("✅ Image uploaded successfully to Firebase: {} -> {}",
                    file.getOriginalFilename(), uploadResult.get("secure_url"));

            return ApiResponse.<Map<String, Object>>builder()
                    .code(1000)
                    .message("Image uploaded successfully to Firebase Storage")
                    .result(result)
                    .build();

        } catch (IOException e) {
            log.error("❌ Firebase upload failed for {}: {}", file.getOriginalFilename(), e.getMessage(), e);
            return ApiResponse.<Map<String, Object>>builder()
                    .code(1005)
                    .message("Upload failed: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("❌ Unexpected error during upload: {}", e.getMessage(), e);
            return ApiResponse.<Map<String, Object>>builder()
                    .code(1005)
                    .message("Upload failed: Unexpected error - " + e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/images/{publicId:.+}")
    @Operation(summary = "Delete Image", description = "Delete an image from Firebase Storage using its public ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteImage(@PathVariable String publicId) {
        try {
            // Decode URL-encoded public_id
            String decodedPublicId = URLDecoder.decode(publicId, StandardCharsets.UTF_8);
            log.info("Deleting Firebase image: {}", decodedPublicId);

            // Use Firebase service to delete the file
            boolean deleted = firebaseStorageService.deleteFile(decodedPublicId);

            if (deleted) {
                return ApiResponse.<String>builder()
                        .code(1000)
                        .message("Image deleted successfully")
                        .result("Deleted: " + decodedPublicId)
                        .build();
            } else {
                log.warn("Firebase delete failed for publicId: {}", decodedPublicId);
                return ApiResponse.<String>builder()
                        .code(1006)
                        .message("Image not found or already deleted")
                        .build();
            }

        } catch (Exception e) {
            log.error("Firebase delete failed", e);
            return ApiResponse.<String>builder()
                    .code(1005)
                    .message("Delete failed: " + e.getMessage())
                    .build();
        }
    }

    // Alternative delete endpoint using request body instead of path variable
    @DeleteMapping("/images")
    @Operation(summary = "Delete Image by Body", description = "Delete an image from Firebase Storage using its public ID in the request body")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteImageByBody(@RequestBody Map<String, String> request) {
        try {
            String publicId = request.get("publicId");
            if (publicId == null || publicId.trim().isEmpty()) {
                return ApiResponse.<String>builder()
                        .code(1004)
                        .message("Public ID is required")
                        .build();
            }

            log.info("Deleting Firebase image via body: {}", publicId);

            // Use Firebase service to delete the file
            boolean deleted = firebaseStorageService.deleteFile(publicId);

            if (deleted) {
                return ApiResponse.<String>builder()
                        .code(1000)
                        .message("Image deleted successfully")
                        .result("Deleted: " + publicId)
                        .build();
            } else {
                log.warn("Firebase delete failed for publicId: {}", publicId);
                return ApiResponse.<String>builder()
                        .code(1006)
                        .message("Image not found or already deleted")
                        .build();
            }

        } catch (Exception e) {
            log.error("Firebase delete failed", e);
            return ApiResponse.<String>builder()
                    .code(1005)
                    .message("Delete failed: " + e.getMessage())
                    .build();
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}