package com.fpt.careermate.services.recruiter_services.web.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fpt.careermate.common.exception.GlobalExceptionHandler;
import com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest;
import com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest.OrganizationInfo;
import com.fpt.careermate.services.recruiter_services.service.RecruiterImp;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterCreationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ContextConfiguration(classes = {RecruiterController.class, GlobalExceptionHandler.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class RecruiterControllerDiffblueTest {
  @Autowired private GlobalExceptionHandler globalExceptionHandler;

  @Autowired private RecruiterController recruiterController;

  @MockitoBean private RecruiterImp recruiterImp;

  /**
   * Test {@link RecruiterController#createRecruiter(RecruiterCreationRequest)}.
   *
   * <ul>
   *   <li>Given {@link Float#NaN}.
   *   <li>Then content string {@code {"code":9999,"message":"Uncategorized error"}}.
   * </ul>
   *
   * <p>Method under test: {@link RecruiterController#createRecruiter(RecruiterCreationRequest)}
   */
  @Test
  @DisplayName(
      "Test createRecruiter(RecruiterCreationRequest); given NaN; then content string '{\"code\":9999,\"message\":\"Uncategorized error\"}'")
  @Tag("MaintainedByDiffblue")
  void testCreateRecruiter_givenNaN_thenContentStringCode9999MessageUncategorizedError()
      throws Exception {
    // Arrange
    RecruiterCreationRequest recruiterCreationRequest = new RecruiterCreationRequest();
    recruiterCreationRequest.setAbout("About");
    recruiterCreationRequest.setCompanyAddress("42 Main St");
    recruiterCreationRequest.setCompanyEmail("jane.doe@example.org");
    recruiterCreationRequest.setCompanyName("Company Name");
    recruiterCreationRequest.setContactPerson("Contact Person");
    recruiterCreationRequest.setLogoUrl("https://example.org/example");
    recruiterCreationRequest.setPhoneNumber("6625550144");
    recruiterCreationRequest.setRating(Float.NaN);
    recruiterCreationRequest.setWebsite("Website");

    MockHttpServletRequestBuilder requestBuilder =
        MockMvcRequestBuilders.post("/api/recruiter")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                JsonMapper.builder()
                    .findAndAddModules()
                    .build()
                    .writeValueAsString(recruiterCreationRequest));

    // Act and Assert
    MockMvcBuilders.standaloneSetup(recruiterController)
        .setControllerAdvice(globalExceptionHandler)
        .build()
        .perform(requestBuilder)
        .andExpect(status().is(400))
        .andExpect(content().contentType("application/json"))
        .andExpect(content().string("{\"code\":9999,\"message\":\"Uncategorized error\"}"));
  }

  /**
   * Test {@link RecruiterController#updateOrganizationInfo(OrganizationInfo)}.
   *
   * <p>Method under test: {@link
   * RecruiterController#updateOrganizationInfo(RecruiterRegistrationRequest.OrganizationInfo)}
   */
  @Test
  @DisplayName("Test updateOrganizationInfo(OrganizationInfo)")
  @Tag("MaintainedByDiffblue")
  void testUpdateOrganizationInfo() throws Exception {
    // Arrange
    OrganizationInfo organizationInfo = new OrganizationInfo();
    organizationInfo.setAbout("About");
    organizationInfo.setCompanyAddress("42 Main St");
    organizationInfo.setCompanyEmail("jane.doe@example.org");
    organizationInfo.setCompanyName("Company Name");
    organizationInfo.setContactPerson("Contact Person");
    organizationInfo.setLogoUrl("https://example.org/example");
    organizationInfo.setPhoneNumber("6625550144");
    organizationInfo.setWebsite("Website");

    MockHttpServletRequestBuilder requestBuilder =
        MockMvcRequestBuilders.put("/api/recruiter/update-organization")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                JsonMapper.builder()
                    .findAndAddModules()
                    .build()
                    .writeValueAsString(organizationInfo));

    // Act and Assert
    MockMvcBuilders.standaloneSetup(recruiterController)
        .setControllerAdvice(globalExceptionHandler)
        .build()
        .perform(requestBuilder)
        .andExpect(status().is(400))
        .andExpect(content().contentType("application/json"))
        .andExpect(
            content()
                .string(
                    "{\"message\":\"Validation failed\",\"errors\":{\"website\":\"Invalid Website URL\"},\"status\":400}"));
  }
}
