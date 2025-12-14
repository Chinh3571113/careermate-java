package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForRecruiterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RecruiterJobPostingRedisService
 */
@ExtendWith(MockitoExtension.class)
class RecruiterJobPostingRedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RecruiterJobPostingRedisService redisService;

    private JobPostingForRecruiterResponse testResponse;
    private static final int TEST_JOB_ID = 123;
    private static final String EXPECTED_KEY = "job_posting:recruiter:123";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        testResponse = JobPostingForRecruiterResponse.builder()
                .id(TEST_JOB_ID)
                .title("Senior Java Developer")
                .description("Great opportunity")
                .address("Ho Chi Minh City")
                .status("ACTIVE")
                .expirationDate(LocalDate.now().plusDays(30))
                .postTime(LocalDate.now())
                .skills(Collections.emptySet())
                .yearsOfExperience(5)
                .workModel("Hybrid")
                .salaryRange("1000-2000 USD")
                .jobPackage("Premium")
                .saved(false)
                .build();
    }

    @Test
    void getFromCache_WhenCacheHit_ReturnsResponse() {
        // Arrange
        when(valueOperations.get(EXPECTED_KEY)).thenReturn(testResponse);

        // Act
        JobPostingForRecruiterResponse result = redisService.getFromCache(TEST_JOB_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_JOB_ID, result.getId());
        assertEquals("Senior Java Developer", result.getTitle());
        verify(valueOperations).get(EXPECTED_KEY);
    }

    @Test
    void getFromCache_WhenCacheMiss_ReturnsNull() {
        // Arrange
        when(valueOperations.get(EXPECTED_KEY)).thenReturn(null);

        // Act
        JobPostingForRecruiterResponse result = redisService.getFromCache(TEST_JOB_ID);

        // Assert
        assertNull(result);
        verify(valueOperations).get(EXPECTED_KEY);
    }

    @Test
    void getFromCache_WhenException_ReturnsNull() {
        // Arrange
        when(valueOperations.get(EXPECTED_KEY)).thenThrow(new RuntimeException("Redis error"));

        // Act
        JobPostingForRecruiterResponse result = redisService.getFromCache(TEST_JOB_ID);

        // Assert
        assertNull(result);
    }

    @Test
    void saveToCache_WithDefaultTTL_SavesCorrectly() {
        // Act
        redisService.saveToCache(TEST_JOB_ID, testResponse);

        // Assert
        verify(valueOperations).set(eq(EXPECTED_KEY), eq(testResponse), eq(60L), eq(TimeUnit.MINUTES));
    }

    @Test
    void saveToCache_WithCustomTTL_SavesCorrectly() {
        // Act
        redisService.saveToCache(TEST_JOB_ID, testResponse, 30, TimeUnit.MINUTES);

        // Assert
        verify(valueOperations).set(EXPECTED_KEY, testResponse, 30L, TimeUnit.MINUTES);
    }

    @Test
    void deleteFromCache_CallsRedisDelete() {
        // Arrange
        when(redisTemplate.delete(EXPECTED_KEY)).thenReturn(true);

        // Act
        redisService.deleteFromCache(TEST_JOB_ID);

        // Assert
        verify(redisTemplate).delete(EXPECTED_KEY);
    }

    @Test
    void deleteMultipleFromCache_DeletesAllIds() {
        // Arrange
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // Act
        redisService.deleteMultipleFromCache(123, 456, 789);

        // Assert
        verify(redisTemplate, times(3)).delete(anyString());
    }

    @Test
    void existsInCache_WhenExists_ReturnsTrue() {
        // Arrange
        when(redisTemplate.hasKey(EXPECTED_KEY)).thenReturn(true);

        // Act
        boolean result = redisService.existsInCache(TEST_JOB_ID);

        // Assert
        assertTrue(result);
        verify(redisTemplate).hasKey(EXPECTED_KEY);
    }

    @Test
    void existsInCache_WhenNotExists_ReturnsFalse() {
        // Arrange
        when(redisTemplate.hasKey(EXPECTED_KEY)).thenReturn(false);

        // Act
        boolean result = redisService.existsInCache(TEST_JOB_ID);

        // Assert
        assertFalse(result);
        verify(redisTemplate).hasKey(EXPECTED_KEY);
    }

    @Test
    void updateCacheTTL_UpdatesExpiration() {
        // Arrange
        when(redisTemplate.expire(EXPECTED_KEY, 120L, TimeUnit.MINUTES)).thenReturn(true);

        // Act
        redisService.updateCacheTTL(TEST_JOB_ID, 120, TimeUnit.MINUTES);

        // Assert
        verify(redisTemplate).expire(EXPECTED_KEY, 120L, TimeUnit.MINUTES);
    }
}

