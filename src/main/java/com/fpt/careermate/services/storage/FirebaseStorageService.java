package com.fpt.careermate.services.storage;

import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Firebase Storage service that provides file upload, delete, and URL
 * generation
 */
@Service
@Slf4j
public class FirebaseStorageService {

    @Value("${firebase.bucket-name}")
    private String bucketName;

    /**
     * Upload file to Firebase Storage and return Cloudinary-compatible response
     */
    public Map<String, Object> uploadFile(MultipartFile file, String folder) throws IOException {
        try {
            // Generate unique filename
            String uniqueId = UUID.randomUUID().toString();
            String originalName = file.getOriginalFilename();
            String extension = getFileExtension(originalName);
            String fileName = folder + "/" + uniqueId + "." + extension;

            // Get Firebase Storage bucket
            Storage storage = StorageClient.getInstance().bucket().getStorage();

            // Create blob info with public read access
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                    .setContentType(file.getContentType())
                    .build();

            // Upload file with options to ensure immediate availability
            Blob blob = storage.create(blobInfo, file.getBytes(),
                    Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));

            // Verify blob was created successfully
            if (blob == null || !blob.exists()) {
                throw new IOException("Failed to create blob in Firebase Storage");
            }

            // Double-check the blob is publicly accessible by setting ACL explicitly
            try {
                blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
            } catch (Exception aclException) {
                log.warn("ACL already set or cannot be modified: {}", aclException.getMessage());
                // Continue anyway as the file was uploaded with PUBLIC_READ
            }

            // Get public URL
            String publicUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);

            log.info("File uploaded successfully to Firebase: {} -> {}", originalName, fileName);
            log.info("File is publicly accessible at: {}", publicUrl);

            // Return Cloudinary-compatible response format
            Map<String, Object> result = new HashMap<>();
            result.put("public_id", fileName);
            result.put("secure_url", publicUrl);
            result.put("url", publicUrl);
            result.put("bytes", file.getSize());
            result.put("format", extension);
            result.put("original_filename", originalName);
            result.put("created_at", System.currentTimeMillis());
            result.put("width", 800); // Default values - could be enhanced to get actual dimensions
            result.put("height", 600);

            return result;

        } catch (Exception e) {
            log.error("Failed to upload file to Firebase: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to Firebase Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Delete file from Firebase Storage
     */
    public boolean deleteFile(String publicId) {
        try {
            // Get Firebase Storage bucket
            Storage storage = StorageClient.getInstance().bucket().getStorage();

            // Delete the blob
            boolean deleted = storage.delete(bucketName, publicId);

            if (deleted) {
                log.info("File deleted successfully from Firebase: {}", publicId);
            } else {
                log.warn("File not found or already deleted: {}", publicId);
            }

            return deleted;

        } catch (Exception e) {
            log.error("Failed to delete file from Firebase: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get public URL for a file
     */
    public String getFileUrl(String publicId) {
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, publicId);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}