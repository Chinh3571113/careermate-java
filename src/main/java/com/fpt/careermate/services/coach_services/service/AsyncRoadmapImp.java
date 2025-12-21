package com.fpt.careermate.services.coach_services.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AsyncRoadmapImp {

    RoadmapImp roadmapImp;

    /**
     * Asynchronously highlight a resume by its ID.
     * @param resumeId the ID of the resume to be highlighted
     *
     */
    @Async("roadmapTaskExecutor")
    public void highlightedResumeAsync(int resumeId) {
        try {
            roadmapImp.highlightedResume(resumeId);
        } catch (Exception e) {
            log.error("highlightedResume failed: "+e.getMessage()+ e);
        }
    }
}

