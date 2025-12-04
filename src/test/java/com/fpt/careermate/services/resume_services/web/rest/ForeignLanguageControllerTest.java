package com.fpt.careermate.services.resume_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.resume_services.service.ForeignLanguageImp;
import com.fpt.careermate.services.resume_services.service.dto.request.ForeignLanguageRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.ForeignLanguageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForeignLanguageController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ForeignLanguageController Tests")
class ForeignLanguageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private ForeignLanguageImp foreignLanguageImp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private ForeignLanguageRequest createValidForeignLanguageRequest() {
        return ForeignLanguageRequest.builder()
                .resumeId(1)
                .language("English")
                .level("Advanced")
                .build();
    }

    @Nested
    @DisplayName("POST /api/foreign-language")
    class AddForeignLanguageTests {

        @Test
        @DisplayName("Should add foreign language successfully")
        void shouldAddForeignLanguageSuccessfully() throws Exception {
            ForeignLanguageRequest request = createValidForeignLanguageRequest();
            ForeignLanguageResponse response = new ForeignLanguageResponse();

            when(foreignLanguageImp.addForeignLanguageToResume(any(ForeignLanguageRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/foreign-language")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Add foreign language successfully"));

            verify(foreignLanguageImp).addForeignLanguageToResume(any(ForeignLanguageRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/foreign-language/{resumeId}/{foreignLanguageId}")
    class UpdateForeignLanguageTests {

        @Test
        @DisplayName("Should update foreign language successfully")
        void shouldUpdateForeignLanguageSuccessfully() throws Exception {
            ForeignLanguageRequest request = createValidForeignLanguageRequest();
            ForeignLanguageResponse response = new ForeignLanguageResponse();

            when(foreignLanguageImp.updateForeignLanguageInResume(eq(1), eq(2), any(ForeignLanguageRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/foreign-language/1/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update foreign language successfully"));

            verify(foreignLanguageImp).updateForeignLanguageInResume(eq(1), eq(2), any(ForeignLanguageRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/foreign-language/{foreignLanguageId}")
    class RemoveForeignLanguageTests {

        @Test
        @DisplayName("Should remove foreign language successfully")
        void shouldRemoveForeignLanguageSuccessfully() throws Exception {
            doNothing().when(foreignLanguageImp).removeForeignLanguageFromResume(0, 1);

            mockMvc.perform(delete("/api/foreign-language/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Remove foreign language successfully"));

            verify(foreignLanguageImp).removeForeignLanguageFromResume(0, 1);
        }
    }
}
