package com.fpt.careermate.services.coach_services.service;

import com.fpt.careermate.services.coach_services.service.dto.response.ResumeRoadmapPageResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RoadmapResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis caching service for Roadmap operations
 * Handles caching for candidate roadmap queries
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoadmapRedisImp {

    RedisTemplate<String, Object> redisTemplate;

    // Cache key prefix for candidate roadmap
    static String CANDIDATE_ROADMAP_PREFIX = "roadmap:candidate:";

    // Cache key prefix for resume roadmap list
    static String RESUME_ROADMAP_LIST_PREFIX = "roadmap:list:candidate:";

    // Cache key prefix for specific resume roadmap list
    static String SPECIFIC_RESUME_ROADMAP_LIST_PREFIX = "roadmap:list:resume:";

    // Cache TTL: 30 minutes (roadmap data doesn't change often)
    static long CACHE_TTL = 30;
    static TimeUnit CACHE_TTL_UNIT = TimeUnit.MINUTES;

    /**
     * Generate cache key for candidate roadmap
     * @param resumeId resume ID
     * @param roadmapName roadmap name
     * @return cache key
     */
    private String getCandidateRoadmapCacheKey(int resumeId, String roadmapName) {
        String sanitizedName = roadmapName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        return CANDIDATE_ROADMAP_PREFIX + "resume:" + resumeId + ":name:" + sanitizedName;
    }

    /**
     * Get candidate roadmap from cache
     * @param resumeId resume ID
     * @param roadmapName roadmap name
     * @return cached RoadmapResponse or null if not found
     */
    public RoadmapResponse getCandidateRoadmapFromCache(int resumeId, String roadmapName) {
        try {
            String key = getCandidateRoadmapCacheKey(resumeId, roadmapName);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                if (cached instanceof RoadmapResponse) {
                    log.debug("Cache hit for candidate roadmap: resumeId={}, roadmapName={}", resumeId, roadmapName);
                    return (RoadmapResponse) cached;
                } else {
                    log.warn("Wrong type in cache, deleting: {}", cached.getClass().getName());
                    redisTemplate.delete(key);
                    return null;
                }
            }

            log.debug("Cache miss for candidate roadmap: resumeId={}, roadmapName={}", resumeId, roadmapName);
            return null;

        } catch (ClassCastException e) {
            log.error("ClassCastException in cache: {}", e.getMessage());
            try {
                String key = getCandidateRoadmapCacheKey(resumeId, roadmapName);
                redisTemplate.delete(key);
            } catch (Exception ex) {
                log.error("Error clearing invalid cache: {}", ex.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting candidate roadmap from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save candidate roadmap to cache
     * @param resumeId resume ID
     * @param roadmapName roadmap name
     * @param response the response to cache
     */
    public void saveCandidateRoadmapToCache(int resumeId, String roadmapName, RoadmapResponse response) {
        try {
            String key = getCandidateRoadmapCacheKey(resumeId, roadmapName);
            redisTemplate.opsForValue().set(key, response, CACHE_TTL, CACHE_TTL_UNIT);
            log.debug("Saved to cache: resumeId={}, roadmapName={}", resumeId, roadmapName);
        } catch (Exception e) {
            log.error("Error saving candidate roadmap to cache: {}", e.getMessage());
        }
    }

    /**
     * Invalidate cache for a specific candidate roadmap
     * @param resumeId resume ID
     * @param roadmapName roadmap name
     */
    public void invalidateCandidateRoadmapCache(int resumeId, String roadmapName) {
        try {
            String key = getCandidateRoadmapCacheKey(resumeId, roadmapName);
            redisTemplate.delete(key);
            log.debug("Invalidated cache: resumeId={}, roadmapName={}", resumeId, roadmapName);
        } catch (Exception e) {
            log.error("Error invalidating candidate roadmap cache: {}", e.getMessage());
        }
    }

    /**
     * Invalidate all roadmap caches for a specific resume
     * @param resumeId resume ID
     */
    public void invalidateAllRoadmapCacheForResume(int resumeId) {
        try {
            String pattern = CANDIDATE_ROADMAP_PREFIX + "resume:" + resumeId + ":*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Invalidated {} cache entries for resumeId={}", keys.size(), resumeId);
            }
        } catch (Exception e) {
            log.error("Error invalidating all roadmap cache for resume: {}", e.getMessage());
        }
    }

    /**
     * Generate cache key for resume roadmap list (by candidateId - all resumes)
     * @param candidateId candidate ID
     * @param page page number
     * @param size page size
     * @param sortBy sort option
     * @return cache key
     */
    private String getCandidateRoadmapListCacheKey(int candidateId, int page, int size, String sortBy) {
        return RESUME_ROADMAP_LIST_PREFIX + candidateId + ":page:" + page + ":size:" + size + ":sort:" + sortBy;
    }

    /**
     * Generate cache key for specific resume roadmap list (by resumeId)
     * @param resumeId resume ID
     * @param page page number
     * @param size page size
     * @param sortBy sort option
     * @return cache key
     */
    private String getSpecificResumeRoadmapListCacheKey(int resumeId, int page, int size, String sortBy) {
        return SPECIFIC_RESUME_ROADMAP_LIST_PREFIX + resumeId + ":page:" + page + ":size:" + size + ":sort:" + sortBy;
    }

    /**
     * Get resume roadmap list from cache (by candidateId - all resumes)
     * @param candidateId candidate ID
     * @param page page number
     * @param size page size
     * @param sortBy sort option
     * @return cached ResumeRoadmapPageResponse or null if not found
     */
    public ResumeRoadmapPageResponse getResumeRoadmapListFromCache(int candidateId, int page, int size, String sortBy) {
        try {
            String key = getCandidateRoadmapListCacheKey(candidateId, page, size, sortBy);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                if (cached instanceof ResumeRoadmapPageResponse) {
                    log.debug("Cache hit for candidate roadmap list: candidateId={}, page={}, size={}, sortBy={}",
                            candidateId, page, size, sortBy);
                    return (ResumeRoadmapPageResponse) cached;
                } else {
                    log.warn("Wrong type in cache, deleting: {}", cached.getClass().getName());
                    redisTemplate.delete(key);
                    return null;
                }
            }

            log.debug("Cache miss for candidate roadmap list: candidateId={}, page={}, size={}, sortBy={}",
                    candidateId, page, size, sortBy);
            return null;

        } catch (ClassCastException e) {
            log.error("ClassCastException in cache: {}", e.getMessage());
            try {
                String key = getCandidateRoadmapListCacheKey(candidateId, page, size, sortBy);
                redisTemplate.delete(key);
            } catch (Exception ex) {
                log.error("Error clearing invalid cache: {}", ex.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting candidate roadmap list from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get specific resume roadmap list from cache (by resumeId)
     * @param resumeId resume ID
     * @param page page number
     * @param size page size
     * @param sortBy sort option
     * @return cached ResumeRoadmapPageResponse or null if not found
     */
    public ResumeRoadmapPageResponse getResumeRoadmapListFromCache(int resumeId, int page, int size, String sortBy, boolean isSpecificResume) {
        if (!isSpecificResume) {
            return getResumeRoadmapListFromCache(resumeId, page, size, sortBy);
        }

        try {
            String key = getSpecificResumeRoadmapListCacheKey(resumeId, page, size, sortBy);
            Object cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                if (cached instanceof ResumeRoadmapPageResponse) {
                    log.debug("Cache hit for specific resume roadmap list: resumeId={}, page={}, size={}, sortBy={}",
                            resumeId, page, size, sortBy);
                    return (ResumeRoadmapPageResponse) cached;
                } else {
                    log.warn("Wrong type in cache, deleting: {}", cached.getClass().getName());
                    redisTemplate.delete(key);
                    return null;
                }
            }

            log.debug("Cache miss for specific resume roadmap list: resumeId={}, page={}, size={}, sortBy={}",
                    resumeId, page, size, sortBy);
            return null;

        } catch (ClassCastException e) {
            log.error("ClassCastException in cache: {}", e.getMessage());
            try {
                String key = getSpecificResumeRoadmapListCacheKey(resumeId, page, size, sortBy);
                redisTemplate.delete(key);
            } catch (Exception ex) {
                log.error("Error clearing invalid cache: {}", ex.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting specific resume roadmap list from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save resume roadmap list to cache (by candidateId - all resumes)
     * @param candidateId candidate ID
     * @param page page number
     * @param size page size
     * @param sortBy sort option
     * @param response the response to cache
     */
    public void saveResumeRoadmapListToCache(int candidateId, int page, int size, String sortBy,
                                              ResumeRoadmapPageResponse response) {
        try {
            String key = getCandidateRoadmapListCacheKey(candidateId, page, size, sortBy);
            redisTemplate.opsForValue().set(key, response, CACHE_TTL, CACHE_TTL_UNIT);
            log.debug("Saved to cache: candidateId={}, page={}, size={}, sortBy={}",
                    candidateId, page, size, sortBy);
        } catch (Exception e) {
            log.error("Error saving candidate roadmap list to cache: {}", e.getMessage());
        }
    }

    /**
     * Save specific resume roadmap list to cache (by resumeId)
     * @param resumeId resume ID
     * @param page page number
     * @param size page size
     * @param sortBy sort option
     * @param response the response to cache
     */
    public void saveResumeRoadmapListToCache(int resumeId, int page, int size, String sortBy,
                                              ResumeRoadmapPageResponse response, boolean isSpecificResume) {
        if (!isSpecificResume) {
            saveResumeRoadmapListToCache(resumeId, page, size, sortBy, response);
            return;
        }

        try {
            String key = getSpecificResumeRoadmapListCacheKey(resumeId, page, size, sortBy);
            redisTemplate.opsForValue().set(key, response, CACHE_TTL, CACHE_TTL_UNIT);
            log.debug("Saved to cache: resumeId={}, page={}, size={}, sortBy={}",
                    resumeId, page, size, sortBy);
        } catch (Exception e) {
            log.error("Error saving specific resume roadmap list to cache: {}", e.getMessage());
        }
    }

    /**
     * Invalidate all resume roadmap list cache for a specific candidate
     * Used when roadmap list changes (add/remove/update)
     * @param candidateId candidate ID
     */
    public void invalidateResumeRoadmapListCache(int candidateId) {
        try {
            String pattern = RESUME_ROADMAP_LIST_PREFIX + candidateId + ":*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Invalidated {} resume roadmap list cache entries for candidateId={}", keys.size(), candidateId);
            }
        } catch (Exception e) {
            log.error("Error invalidating resume roadmap list cache: {}", e.getMessage());
        }
    }

    /**
     * Invalidate specific resume roadmap list cache
     * Used when roadmap list of specific resume changes
     * @param resumeId resume ID
     */
    public void invalidateSpecificResumeRoadmapListCache(int resumeId) {
        try {
            String pattern = SPECIFIC_RESUME_ROADMAP_LIST_PREFIX + resumeId + ":*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Invalidated {} specific resume roadmap list cache entries for resumeId={}", keys.size(), resumeId);
            }
        } catch (Exception e) {
            log.error("Error invalidating specific resume roadmap list cache: {}", e.getMessage());
        }
    }
}


