package com.fpt.careermate.services;

import com.fpt.careermate.services.dto.request.HighlightProjectRequest;
import com.fpt.careermate.services.dto.response.HighlightProjectResponse;
import com.fpt.careermate.services.impl.HighLightProjectService;

public class HighLightProjectImp implements HighLightProjectService {
    @Override
    public HighlightProjectResponse addHighlightProjectToResume(HighlightProjectRequest highlightProject) {
        return null;
    }

    @Override
    public void removeHighlightProjectFromResume(int resumeId, int highlightProjectId) {

    }

    @Override
    public HighlightProjectResponse updateHighlightProjectInResume(int resumeId, HighlightProjectRequest highlightProject) {
        return null;
    }
}
