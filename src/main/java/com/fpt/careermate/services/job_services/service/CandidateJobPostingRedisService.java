package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis caching service for Candidate Job Posting operations
 * Handles caching for public/candidate job posting queries
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateJobPostingRedisService {

    RedisTemplate<String, Object> redisTemplate;

    // Cache key prefix for candidate (public) job postings
    private static final String CANDIDATE_LIST_PREFIX = "job_posting:candidate:list:";

    // List cache TTL: 5 minutes (shorter because list changes more often)
    private static final long LIST_TTL = 5;
    private static final TimeUnit LIST_TTL_UNIT = TimeUnit.MINUTES;

    /**
     * Generate cache key for candidate (public) job posting list
     * @param page page number
     * @param size page size
     * @param keyword search keyword
     * @return cache key
     */
    private String getCandidateListCacheKey(int page, int size, String keyword) {
        String keywordPart = (keyword == null || keyword.isEmpty()) ? "all" : keyword.replaceAll("[^a-zA-Z0-9]", "_");
        return CANDIDATE_LIST_PREFIX + "p" + page + ":s" + size + ":k" + keywordPart;
    }

    /**
     * Get approved job posting list from cache (for candidates)
     * @param page page number
     * @param size page size
     * @param keyword search keyword
     * @return cached PageResponse or null if not found
     */
    @SuppressWarnings("unchecked")
    public PageResponse<?> getCandidateListFromCache(int page, int size, String keyword) {
        try {
            String key = getCandidateListCacheKey(page, size, keyword);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                // Check if it's the right type before casting
                if (cached instanceof PageResponse) {
                    PageResponse<?> result = (PageResponse<?>) cached;

                    return result;
                } else {
                    log.warn("Wrong type in cache, deleting: {}", cached.getClass().getName());
                    redisTemplate.delete(key);
                    return null;
                }
            }

            return null;

        } catch (ClassCastException e) {
            log.error("ClassCastException in cache: {}", e.getMessage());
            // Clear the invalid cache entry
            try {
                String key = getCandidateListCacheKey(page, size, keyword);
                redisTemplate.delete(key);
            } catch (Exception ex) {
                log.error("Error clearing invalid cache: {}", ex.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting candidate job posting list from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save approved job posting list to cache (for candidates)
     * @param page page number
     * @param size page size
     * @param keyword search keyword
     * @param response the response to cache
     */
    public void saveCandidateListToCache(int page, int size, String keyword, PageResponse<?> response) {
        try {
            String key = getCandidateListCacheKey(page, size, keyword);
            redisTemplate.opsForValue().set(key, response, LIST_TTL, LIST_TTL_UNIT);
        } catch (Exception e) {
            log.error("Error saving candidate job posting list to cache: {}", e.getMessage());
        }
    }

    /**
     * Clear all candidate job posting list caches
     * Call this when any approved job posting is created, updated, deleted, or status changed
     */
    public void clearAllCandidateListCache() {
        try {
            String pattern = CANDIDATE_LIST_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Error clearing candidate list cache: {}", e.getMessage());
        }
    }
}

