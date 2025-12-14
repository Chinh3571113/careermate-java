package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.response.AdminPageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis caching service for Admin Job Posting operations
 * Handles caching for admin-specific job posting queries
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminJobPostingRedisService {

    RedisTemplate<String, Object> redisTemplate;

    // Cache key prefix for admin job postings
    private static final String ADMIN_LIST_PREFIX = "job_posting:admin:list:";
    private static final String PENDING_JOBS_KEY = "job_posting:admin:pending";

    // List cache TTL: 5 minutes (shorter because list changes more often)
    private static final long LIST_TTL = 5;
    private static final TimeUnit LIST_TTL_UNIT = TimeUnit.MINUTES;

    /**
     * Generate cache key for admin job posting list
     * @param page page number
     * @param size page size
     * @param status filter status
     * @param sortBy sort field
     * @param sortDirection sort direction
     * @return cache key
     */
    private String getAdminListCacheKey(int page, int size, String status, String sortBy, String sortDirection) {
        String statusPart = (status == null || status.isEmpty() || status.equalsIgnoreCase("ALL"))
            ? "all"
            : status.toLowerCase();
        String sortPart = sortBy + "_" + sortDirection.toLowerCase();
        return ADMIN_LIST_PREFIX + "p" + page + ":s" + size + ":st" + statusPart + ":sort" + sortPart;
    }

    /**
     * Get admin job posting list from cache
     * @param page page number
     * @param size page size
     * @param status filter status
     * @param sortBy sort field
     * @param sortDirection sort direction
     * @return cached Page or null if not found
     */
    @SuppressWarnings("unchecked")
    public Page<?> getAdminListFromCache(
            int page, int size, String status, String sortBy, String sortDirection) {
        try {
            String key = getAdminListCacheKey(page, size, status, sortBy, sortDirection);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                if (cached instanceof AdminPageResponse) {
                    AdminPageResponse<?> adminPageResponse = (AdminPageResponse<?>) cached;
                    Page<?> result = adminPageResponse.toPage();

                    return result;
                } else {
                    log.warn("Wrong type in admin cache, deleting: {}", cached.getClass().getName());
                    redisTemplate.delete(key);
                    return null;
                }
            }

            return null;

        } catch (ClassCastException e) {
            log.error("ClassCastException in admin cache: {}", e.getMessage());
            try {
                String key = getAdminListCacheKey(page, size, status, sortBy, sortDirection);
                redisTemplate.delete(key);
            } catch (Exception ex) {
                log.error("Error clearing invalid admin cache: {}", ex.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting admin job posting list from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save admin job posting list to cache
     * @param page page number
     * @param size page size
     * @param status filter status
     * @param sortBy sort field
     * @param sortDirection sort direction
     * @param response the Page to cache
     */
    public void saveAdminListToCache(
            int page, int size, String status, String sortBy, String sortDirection,
            Page<?> response) {
        try {
            String key = getAdminListCacheKey(page, size, status, sortBy, sortDirection);

            // Convert Spring Page to AdminPageResponse for serialization
            AdminPageResponse<?> adminPageResponse = AdminPageResponse.from(response);

            redisTemplate.opsForValue().set(key, adminPageResponse, LIST_TTL, LIST_TTL_UNIT);
        } catch (Exception e) {
            log.error("Error saving admin job posting list to cache: {}", e.getMessage());
        }
    }

    /**
     * Clear all admin job posting list caches
     * Call this when any job posting is created, updated, deleted, or status changed
     */
    public void clearAllAdminListCache() {
        try {
            String pattern = ADMIN_LIST_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Error clearing admin list cache: {}", e.getMessage());
        }
    }

    // ==================== PENDING JOB POSTINGS CACHE METHODS ====================

    /**
     * Get pending job postings list from cache
     * @return cached List or null if not found
     */
    @SuppressWarnings("unchecked")
    public List<?> getPendingJobsFromCache() {
        try {
            Object cached = redisTemplate.opsForValue().get(PENDING_JOBS_KEY);

            if (cached != null) {
                if (cached instanceof List) {
                    List<?> result = (List<?>) cached;
                    return result;
                } else {
                    log.warn("Wrong type in pending jobs cache, deleting: {}", cached.getClass().getName());
                    redisTemplate.delete(PENDING_JOBS_KEY);
                    return null;
                }
            }

            return null;

        } catch (ClassCastException e) {
            log.error("ClassCastException in pending jobs cache: {}", e.getMessage());
            try {
                redisTemplate.delete(PENDING_JOBS_KEY);
            } catch (Exception ex) {
                log.error("Error clearing invalid pending jobs cache: {}", ex.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting pending job postings from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save pending job postings list to cache
     * @param pendingJobs the list of pending jobs to cache
     */
    public void savePendingJobsToCache(List<?> pendingJobs) {
        try {
            redisTemplate.opsForValue().set(PENDING_JOBS_KEY, pendingJobs, LIST_TTL, LIST_TTL_UNIT);
        } catch (Exception e) {
            log.error("Error saving pending job postings to cache: {}", e.getMessage());
        }
    }

    /**
     * Clear pending job postings cache
     * Call this when any job posting status changes or new job posting is created
     */
    public void clearPendingJobsCache() {
        try {
            Boolean deleted = redisTemplate.delete(PENDING_JOBS_KEY);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Cleared pending job postings cache");
            }
        } catch (Exception e) {
            log.error("Error clearing pending jobs cache: {}", e.getMessage());
        }
    }
}

