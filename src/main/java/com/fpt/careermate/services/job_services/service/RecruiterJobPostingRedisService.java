package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.job_services.service.dto.response.PageJobPostingForRecruiterResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis caching service for Job Postings (Recruiter & Candidate)
 * Provides reusable methods for caching job posting data
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterJobPostingRedisService {

    RedisTemplate<String, Object> redisTemplate;

    // Cache key prefix for recruiter job postings
    private static final String RECRUITER_PREFIX = "job_posting:recruiter:";
    private static final String RECRUITER_LIST_PREFIX = "job_posting:recruiter:list:";

    // Default TTL: 1 hour
    private static final long DEFAULT_TTL = 60;
    private static final TimeUnit DEFAULT_TTL_UNIT = TimeUnit.MINUTES;

    // List cache TTL: 5 minutes (shorter because list changes more often)
    private static final long LIST_TTL = 5;
    private static final TimeUnit LIST_TTL_UNIT = TimeUnit.MINUTES;

    /**
     * Generate cache key for a job posting
     * @param jobPostingId the job posting ID
     * @return cache key
     */
    private String getCacheKey(int jobPostingId) {
        return RECRUITER_PREFIX + jobPostingId;
    }

    /**
     * Generate cache key for job posting list
     * @param recruiterId recruiter ID
     * @param page page number
     * @param size page size
     * @param keyword search keyword
     * @return cache key
     */
    private String getListCacheKey(int recruiterId, int page, int size, String keyword) {
        String keywordPart = (keyword == null || keyword.isEmpty()) ? "all" : keyword.replaceAll("[^a-zA-Z0-9]", "_");
        return RECRUITER_LIST_PREFIX + recruiterId + ":p" + page + ":s" + size + ":k" + keywordPart;
    }

    /**
     * Get job posting from cache
     * @param jobPostingId the job posting ID
     * @return cached JobPostingForRecruiterResponse or null if not found
     */
    public JobPostingForRecruiterResponse getFromCache(int jobPostingId) {
        try {
            String key = getCacheKey(jobPostingId);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                return (JobPostingForRecruiterResponse) cached;
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting job posting from cache: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Save job posting to cache with default TTL
     * @param jobPostingId the job posting ID
     * @param response the response to cache
     */
    public void saveToCache(int jobPostingId, JobPostingForRecruiterResponse response) {
        saveToCache(jobPostingId, response, DEFAULT_TTL, DEFAULT_TTL_UNIT);
    }

    /**
     * Save job posting to cache with custom TTL
     * @param jobPostingId the job posting ID
     * @param response the response to cache
     * @param ttl time to live
     * @param timeUnit time unit for TTL
     */
    public void saveToCache(int jobPostingId, JobPostingForRecruiterResponse response, long ttl, TimeUnit timeUnit) {
        try {
            String key = getCacheKey(jobPostingId);
            redisTemplate.opsForValue().set(key, response, ttl, timeUnit);
        } catch (Exception e) {
            log.error("Error saving job posting to cache: {}", e.getMessage());
        }
    }

    /**
     * Delete job posting from cache
     * @param jobPostingId the job posting ID
     */
    public void deleteFromCache(int jobPostingId) {
        try {
            String key = getCacheKey(jobPostingId);
            Boolean deleted = redisTemplate.delete(key);

            if (deleted) {
                log.debug("Deleted job posting from cache with ID: {}", jobPostingId);
            } else {
                log.debug("Job posting not found in cache for deletion with ID: {}", jobPostingId);
            }
        } catch (Exception e) {
            log.error("Error deleting job posting from cache: {}", e.getMessage());
        }
    }

    /**
     * Delete multiple job postings from cache
     * @param jobPostingIds the job posting IDs to delete
     */
    public void deleteMultipleFromCache(int... jobPostingIds) {
        for (int jobPostingId : jobPostingIds) {
            deleteFromCache(jobPostingId);
        }
    }

    /**
     * Check if job posting exists in cache
     * @param jobPostingId the job posting ID
     * @return true if exists, false otherwise
     */
    public boolean existsInCache(int jobPostingId) {
        try {
            String key = getCacheKey(jobPostingId);
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking cache existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Update cache TTL for a job posting
     * @param jobPostingId the job posting ID
     * @param ttl new time to live
     * @param timeUnit time unit for TTL
     */
    public void updateCacheTTL(int jobPostingId, long ttl, TimeUnit timeUnit) {
        try {
            String key = getCacheKey(jobPostingId);
            Boolean result = redisTemplate.expire(key, ttl, timeUnit);

            if (result) {
                log.debug("Updated TTL for job posting ID: {} to {} {}", jobPostingId, ttl, timeUnit);
            }
        } catch (Exception e) {
            log.error("Error updating cache TTL: {}", e.getMessage());
        }
    }

    /**
     * Get job posting list from cache
     * @param recruiterId recruiter ID
     * @param page page number
     * @param size page size
     * @param keyword search keyword
     * @return cached PageJobPostingForRecruiterResponse or null if not found
     */
    public PageJobPostingForRecruiterResponse getListFromCache(int recruiterId, int page, int size, String keyword) {
        try {
            String key = getListCacheKey(recruiterId, page, size, keyword);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                log.debug("Cache hit for job posting list - recruiterId: {}, page: {}, size: {}, keyword: {}",
                         recruiterId, page, size, keyword);
                return (PageJobPostingForRecruiterResponse) cached;
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting job posting list from cache: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Save job posting list to cache
     * @param recruiterId recruiter ID
     * @param page page number
     * @param size page size
     * @param keyword search keyword
     * @param response the response to cache
     */
    public void saveListToCache(int recruiterId, int page, int size, String keyword, PageJobPostingForRecruiterResponse response) {
        try {
            String key = getListCacheKey(recruiterId, page, size, keyword);
            redisTemplate.opsForValue().set(key, response, LIST_TTL, LIST_TTL_UNIT);
        } catch (Exception e) {
            log.error("Error saving job posting list to cache: {}", e.getMessage());
        }
    }

    /**
     * Clear all list caches for a specific recruiter
     * Call this when any job posting is created, updated, or deleted
     * @param recruiterId the recruiter ID
     */
    public void clearRecruiterListCache(int recruiterId) {
        try {
            String pattern = RECRUITER_LIST_PREFIX + recruiterId + ":*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Error clearing recruiter list cache: {}", e.getMessage());
        }
    }

    /**
     * Clear all recruiter job posting list caches
     * Call this when any job posting is created, updated, deleted, or status changed
     */
    public void clearAllRecruiterListCache() {
        try {
            String pattern = RECRUITER_LIST_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Error clearing recruiter list cache: {}", e.getMessage());
        }
    }
}
