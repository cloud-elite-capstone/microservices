package com.cartesian.agent_orchestrator_service.controller;

import com.cartesian.agent_orchestrator_service.dto.search.ProductSearchRequest;
import com.cartesian.agent_orchestrator_service.dto.search.SearchResultsResponse;
import com.cartesian.agent_orchestrator_service.service.ProductSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import com.cartesian.agent_orchestrator_service.config.WebMvcConfig;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductSearchOrchestrationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductSearchService searchOrchestratorService;

    @InjectMocks
    private ProductSearchController productSearchOrchestrationController;

    @BeforeEach
    void setUp() {
        FormattingConversionService conversionService = new FormattingConversionService();
        new WebMvcConfig().addFormatters(conversionService);

        mockMvc = MockMvcBuilders.standaloneSetup(productSearchOrchestrationController)
                .setConversionService(conversionService)
                .build();
    }

    @Test
    void searchProductsMultipart_withEmptyImageString_shouldSucceed() throws Exception {
        when(searchOrchestratorService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(new SearchResultsResponse(Collections.emptyList()));

        mockMvc.perform(multipart("/products/search")
                        .param("search", "laptop")
                        .param("image", ""))
                .andExpect(status().isOk());
    }

    @Test
    void searchProductsMultipart_withActualFile_shouldSucceed() throws Exception {
        when(searchOrchestratorService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(new SearchResultsResponse(Collections.emptyList()));

        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        mockMvc.perform(multipart("/products/search")
                        .file(file)
                        .param("search", "laptop"))
                .andExpect(status().isOk());
    }
}
