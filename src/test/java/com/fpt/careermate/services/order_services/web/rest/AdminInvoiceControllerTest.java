package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.services.order_services.service.AdminInvoiceImp;
import com.fpt.careermate.services.order_services.service.dto.response.InvoiceListResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageInvoiceListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminInvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminInvoiceController Tests")
class AdminInvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminInvoiceImp adminInvoiceService;

    @Nested
    @DisplayName("GET /admin/invoices/recruiters - Get All Recruiter Invoices")
    class GetAllRecruiterInvoicesTests {

        @Test
        @DisplayName("Should get all recruiter invoices with filters successfully")
        void getAllRecruiterInvoices_WithFilters_ReturnsSuccess() throws Exception {
            // Arrange
            List<InvoiceListResponse> content = new ArrayList<>();
            InvoiceListResponse invoice = InvoiceListResponse.builder()
                    .id(1)
                    .fullname("John Doe")
                    .packageName("Premium Package")
                    .amount(1000000L)
                    .status("PAID")
                    .isActive(true)
                    .build();
            content.add(invoice);

            PageInvoiceListResponse pageResponse = PageInvoiceListResponse.builder()
                    .content(content)
                    .number(0)
                    .size(5)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(adminInvoiceService.getAllRecruiterInvoices(any(), any(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/admin/invoices/recruiters")
                            .param("status", "PAID")
                            .param("isActive", "true")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Get recruiter invoices successfully"))
                    .andExpect(jsonPath("$.result.content[0].id").value(1))
                    .andExpect(jsonPath("$.result.content[0].status").value("PAID"));

            verify(adminInvoiceService, times(1))
                    .getAllRecruiterInvoices("PAID", true, 0, 5);
        }

        @Test
        @DisplayName("Should get all recruiter invoices without filters successfully")
        void getAllRecruiterInvoices_WithoutFilters_ReturnsSuccess() throws Exception {
            // Arrange
            PageInvoiceListResponse pageResponse = PageInvoiceListResponse.builder()
                    .content(new ArrayList<>())
                    .number(0)
                    .size(5)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(adminInvoiceService.getAllRecruiterInvoices(isNull(), isNull(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/admin/invoices/recruiters")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.content").isEmpty());

            verify(adminInvoiceService, times(1))
                    .getAllRecruiterInvoices(null, null, 0, 5);
        }

        @Test
        @DisplayName("Should get all recruiter invoices with custom pagination")
        void getAllRecruiterInvoices_WithCustomPagination_ReturnsSuccess() throws Exception {
            // Arrange
            PageInvoiceListResponse pageResponse = PageInvoiceListResponse.builder()
                    .content(new ArrayList<>())
                    .number(2)
                    .size(10)
                    .totalElements(25L)
                    .totalPages(3)
                    .first(false)
                    .last(false)
                    .build();

            when(adminInvoiceService.getAllRecruiterInvoices(any(), any(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/admin/invoices/recruiters")
                            .param("status", "PENDING")
                            .param("page", "2")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.number").value(2))
                    .andExpect(jsonPath("$.result.size").value(10));

            verify(adminInvoiceService, times(1))
                    .getAllRecruiterInvoices("PENDING", null, 2, 10);
        }

        @Test
        @DisplayName("Should get all recruiter invoices with isActive filter only")
        void getAllRecruiterInvoices_WithIsActiveFilter_ReturnsSuccess() throws Exception {
            // Arrange
            PageInvoiceListResponse pageResponse = PageInvoiceListResponse.builder()
                    .content(new ArrayList<>())
                    .number(0)
                    .size(5)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(adminInvoiceService.getAllRecruiterInvoices(isNull(), any(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/admin/invoices/recruiters")
                            .param("isActive", "false")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(adminInvoiceService, times(1))
                    .getAllRecruiterInvoices(null, false, 0, 5);
        }
    }

    @Nested
    @DisplayName("GET /admin/invoices/candidates - Get All Candidate Invoices")
    class GetAllCandidateInvoicesTests {

        @Test
        @DisplayName("Should get all candidate invoices with filters successfully")
        void getAllCandidateInvoices_WithFilters_ReturnsSuccess() throws Exception {
            // Arrange
            List<InvoiceListResponse> content = new ArrayList<>();
            InvoiceListResponse invoice = InvoiceListResponse.builder()
                    .id(1)
                    .fullname("Jane Smith")
                    .packageName("Premium CV Package")
                    .amount(500000L)
                    .status("PAID")
                    .isActive(true)
                    .build();
            content.add(invoice);

            PageInvoiceListResponse pageResponse = PageInvoiceListResponse.builder()
                    .content(content)
                    .number(0)
                    .size(5)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(adminInvoiceService.getAllCandidateInvoices(any(), any(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/admin/invoices/candidates")
                            .param("status", "PAID")
                            .param("isActive", "true")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Get candidate invoices successfully"))
                    .andExpect(jsonPath("$.result.content[0].id").value(1))
                    .andExpect(jsonPath("$.result.content[0].status").value("PAID"));

            verify(adminInvoiceService, times(1))
                    .getAllCandidateInvoices("PAID", true, 0, 5);
        }

        @Test
        @DisplayName("Should get all candidate invoices without filters successfully")
        void getAllCandidateInvoices_WithoutFilters_ReturnsSuccess() throws Exception {
            // Arrange
            PageInvoiceListResponse pageResponse = PageInvoiceListResponse.builder()
                    .content(new ArrayList<>())
                    .number(0)
                    .size(5)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(adminInvoiceService.getAllCandidateInvoices(isNull(), isNull(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/admin/invoices/candidates")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.content").isEmpty());

            verify(adminInvoiceService, times(1))
                    .getAllCandidateInvoices(null, null, 0, 5);
        }

        @Test
        @DisplayName("Should get all candidate invoices with custom pagination")
        void getAllCandidateInvoices_WithCustomPagination_ReturnsSuccess() throws Exception {
            // Arrange
            PageInvoiceListResponse pageResponse = PageInvoiceListResponse.builder()
                    .content(new ArrayList<>())
                    .number(1)
                    .size(10)
                    .totalElements(15L)
                    .totalPages(2)
                    .first(false)
                    .last(false)
                    .build();

            when(adminInvoiceService.getAllCandidateInvoices(any(), any(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/admin/invoices/candidates")
                            .param("status", "PENDING")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.number").value(1))
                    .andExpect(jsonPath("$.result.size").value(10));

            verify(adminInvoiceService, times(1))
                    .getAllCandidateInvoices("PENDING", null, 1, 10);
        }

        @Test
        @DisplayName("Should get all candidate invoices with isActive filter only")
        void getAllCandidateInvoices_WithIsActiveFilter_ReturnsSuccess() throws Exception {
            // Arrange
            PageInvoiceListResponse pageResponse = PageInvoiceListResponse.builder()
                    .content(new ArrayList<>())
                    .number(0)
                    .size(5)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(adminInvoiceService.getAllCandidateInvoices(isNull(), any(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/admin/invoices/candidates")
                            .param("isActive", "false")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(adminInvoiceService, times(1))
                    .getAllCandidateInvoices(null, false, 0, 5);
        }
    }
}

