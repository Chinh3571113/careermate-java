package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.storage.FirebaseStorageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class FileController {

    FirebaseStorageService firebaseStorageService;

    // ADMIN ONLY - Image Upload to Firebase Storage
    @PostMapping("/upload/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> uploadImage(@RequestParam("image") MultipartFile file) {
        log.info("Uploading image to Firebase Storage: {}", file.getOriginalFilename());

        // Validate file type
        if (!isImageFile(file)) {
            return ApiResponse.<Map<String, Object>>builder()
                    .code(1004)
                    .message("Only image files are allowed")
                    .build();
        }

        try {
            // Upload to Firebase Storage
            Map<String, Object> uploadResult = firebaseStorageService.uploadFile(file, "careermate/blogs");

            // Prepare response (same format as before)
            Map<String, Object> result = new HashMap<>();
            result.put("imageUrl", uploadResult.get("secure_url"));
            result.put("publicId", uploadResult.get("public_id"));
            result.put("fileSize", file.getSize());
            result.put("originalName", file.getOriginalFilename());
            result.put("width", uploadResult.get("width"));
            result.put("height", uploadResult.get("height"));

            log.info("Image uploaded successfully to Firebase: {}", uploadResult.get("public_id"));

            return ApiResponse.<Map<String, Object>>builder()
                    .code(1000)
                    .message("Image uploaded successfully to Firebase Storage")
                    .result(result)
                    .build();

        } catch (Exception e) {
            log.error("Firebase upload failed: {}", e.getMessage());
            return ApiResponse.<Map<String, Object>>builder()
                    .code(1005)
                    .message("Upload failed: " + e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/images/{publicId:.+}")
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