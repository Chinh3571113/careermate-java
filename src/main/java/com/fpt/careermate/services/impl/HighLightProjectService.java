package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.HighlightProjectRequest;
import com.fpt.careermate.services.dto.response.HighlightProjectResponse;

public interface HighLightProjectService {
    HighlightProjectResponse addHighlightProjectToResume(HighlightProjectRequest highlightProject);
    void removeHighlightProjectFromResume(int resumeId, int highlightProjectId);
    HighlightProjectResponse updateHighlightProjectInResume(int resumeId,HighlightProjectRequest highlightProject);

}
