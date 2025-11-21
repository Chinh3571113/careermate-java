package com.fpt.careermate.services.blog_services.service;

import com.fpt.careermate.services.file_services.service.FileStorageImp;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogImageCleanupImp {

    FileStorageImp fileStorageImp;

    /**
     * Extract image file names from HTML content
     */
    public List<String> extractImageFileNames(String htmlContent) {
        log.info("=== extractImageFileNames START ===");
        log.info("HTML content length: {}", htmlContent != null ? htmlContent.length() : 0);
        if (htmlContent != null && htmlContent.length() > 0) {
            log.info("First 500 chars of content: {}", htmlContent.substring(0, Math.min(500, htmlContent.length())));
        }
        
        List<String> imageFileNames = new ArrayList<>();

        if (htmlContent == null || htmlContent.isEmpty()) {
            log.info("HTML content is null or empty, returning empty list");
            return imageFileNames;
        }

        // Pattern to match image sources in HTML - more flexible
        // Matches: <img src="..."> or <img src='...'> or src="..." or src='...'
        Pattern pattern = Pattern.compile("src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlContent);

        int matchCount = 0;
        while (matcher.find()) {
            matchCount++;
            String imageSrc = matcher.group(1);
            log.info("Found image #{}: {}", matchCount, imageSrc);

            // Extract just the filename from the URL
            // Assuming URLs are like /api/files/{filename} or https://firebase.../filename
            String fileName = imageSrc.substring(imageSrc.lastIndexOf('/') + 1);
            
            // Remove query parameters if any (e.g., ?alt=media&token=...)
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            
            imageFileNames.add(fileName);
            log.info("Extracted filename: {}", fileName);
        }

        log.info("=== extractImageFileNames END === Total images found: {}", imageFileNames.size());
        return imageFileNames;
    }
    
    /**
     * Extract filename from a direct URL (for thumbnails)
     */
    public String extractFileNameFromUrl(String url) {
        log.info("Extracting filename from URL: {}", url);
        
        if (url == null || url.isEmpty()) {
            log.info("URL is null or empty");
            return null;
        }
        
        try {
            // Extract filename from URL path
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            
            // Remove query parameters if any
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            
            log.info("Extracted filename: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("Error extracting filename from URL: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extract file path from Firebase URL for deletion
     * URL format: https://storage.googleapis.com/bucket-name/careermate/blogs/filename.jpg
     * Returns: careermate/blogs/filename.jpg
     */
    public String extractFilePathFromUrl(String url) {
        log.info("Extracting file path from Firebase URL: {}", url);
        
        if (url == null || url.isEmpty()) {
            log.info("URL is null or empty");
            return null;
        }
        
        try {
            // Find the start of the path after bucket name
            // URL format: https://storage.googleapis.com/bucket-name/path/to/file.jpg
            String searchPattern = ".firebasestorage.app/";
            int pathStart = url.indexOf(searchPattern);
            
            if (pathStart == -1) {
                // Try alternative format: googleapis.com/bucket-name/
                searchPattern = ".googleapis.com/";
                pathStart = url.indexOf(searchPattern);
                if (pathStart != -1) {
                    // Skip to after bucket name
                    pathStart = url.indexOf('/', pathStart + searchPattern.length());
                }
            }
            
            if (pathStart == -1) {
                log.warn("Could not find Firebase storage pattern in URL");
                return null;
            }
            
            // Extract path after bucket name
            String filePath = url.substring(pathStart + searchPattern.length());
            
            // Remove query parameters if any
            if (filePath.contains("?")) {
                filePath = filePath.substring(0, filePath.indexOf("?"));
            }
            
            log.info("Extracted file path: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("Error extracting file path from URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Delete images that are no longer referenced in the blog content
     */
    public void cleanupUnusedImages(String oldContent, String newContent) {
        log.info("=== cleanupUnusedImages START ===");
        try {
            List<String> oldImages = extractImageFileNames(oldContent);
            List<String> newImages = extractImageFileNames(newContent);

            log.info("Old images count: {}", oldImages.size());
            log.info("New images count: {}", newImages.size());
            log.info("Old images: {}", oldImages);
            log.info("New images: {}", newImages);

            // Find images that were in old content but not in new content
            List<String> imagesToDelete = new ArrayList<>(oldImages);
            imagesToDelete.removeAll(newImages);

            log.info("Images to delete count: {}", imagesToDelete.size());
            log.info("Images to delete: {}", imagesToDelete);

            // Delete unused images
            for (String fileName : imagesToDelete) {
                log.info("Attempting to delete unused image: {}", fileName);
                fileStorageImp.deleteFile(fileName);
                log.info("Successfully deleted unused image: {}", fileName);
            }

            log.info("=== cleanupUnusedImages END ===");
        } catch (Exception e) {
            log.error("=== cleanupUnusedImages ERROR === {}", e.getMessage(), e);
        }
    }

    /**
     * Delete all images from a blog content (when blog is deleted)
     */
    public void deleteAllImagesFromContent(String htmlContent) {
        log.info("=== deleteAllImagesFromContent START ===");
        try {
            List<String> imageFileNames = extractImageFileNames(htmlContent);
            log.info("Total images to delete: {}", imageFileNames.size());

            for (String fileName : imageFileNames) {
                log.info("Attempting to delete image: {}", fileName);
                fileStorageImp.deleteFile(fileName);
                log.info("Successfully deleted image from deleted blog: {}", fileName);
            }

            log.info("=== deleteAllImagesFromContent END ===");
        } catch (Exception e) {
            log.error("=== deleteAllImagesFromContent ERROR === {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled task to clean up orphaned images (optional)
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledCleanup() {
        log.info("Starting scheduled image cleanup task");
        // This could be enhanced to check all images in storage
        // and compare with images referenced in active blogs
        // For now, it's just a placeholder for future enhancement
    }
}
