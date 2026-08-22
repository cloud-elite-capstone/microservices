package com.cartesian.agentservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.cartesian.agentservice.config.WebMvcConfig;
import com.cartesian.agentservice.dto.search.ProductSearchRequest;
import com.cartesian.agentservice.dto.search.SearchResultsResponse;
import com.cartesian.agentservice.service.ProductSearchService;

@ExtendWith(MockitoExtension.class)
class ProductSearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductSearchService searchService;

    @InjectMocks
    private ProductSearchController productSearchController;

    @BeforeEach
    void setUp() {
        FormattingConversionService conversionService = new FormattingConversionService();
        new WebMvcConfig().addFormatters(conversionService);

        mockMvc = MockMvcBuilders.standaloneSetup(productSearchController)
                .setConversionService(conversionService)
                .build();
    }

    @Test
    void searchProductsMultipart_withEmptyImageString_shouldSucceed() throws Exception {
        when(searchService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(new SearchResultsResponse(Collections.emptyList()));

        mockMvc.perform(multipart("/products/search")
                        .param("search", "laptop")
                        .param("image", ""))
                .andExpect(status().isOk());
    }

    @Test
    void searchProductsMultipart_withActualFile_shouldSucceed() throws Exception {
        when(searchService.searchProducts(any(ProductSearchRequest.class)))
                .thenReturn(new SearchResultsResponse(Collections.emptyList()));

        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        mockMvc.perform(multipart("/products/search")
                        .file(file)
                        .param("search", "laptop"))
                .andExpect(status().isOk());
    }
}
