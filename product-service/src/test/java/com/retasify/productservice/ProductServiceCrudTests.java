package com.retasify.productservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.retasify.productservice.dto.ProductRequest;
import com.retasify.productservice.dto.ProductResponse;
import com.retasify.productservice.exception.ProductNotFoundException;
import com.retasify.productservice.model.Category;
import com.retasify.productservice.repository.CategoryRepository;
import com.retasify.productservice.repository.ProductRepository;
import com.retasify.productservice.service.ProductService;

@SpringBootTest
class ProductServiceCrudTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void productCrudFlow() {
        Category category = categoryRepository.save(new Category("Fruit", "Fresh fruit"));
        UUID shopId = UUID.randomUUID();

        ProductRequest request = new ProductRequest(
                "Apple",
                "Crisp and sweet",
                new BigDecimal("4.99"),
                null,
                12,
                null,
                category.getId(),
                "https://example.com/apple.jpg",
                shopId
        );

        ProductResponse created = productService.createProduct(request);
        assertNotNull(created.id());
        assertEquals("Apple", created.name());

        ProductResponse fetched = productService.getProductById(created.id());
        assertEquals("Crisp and sweet", fetched.description());

        ProductRequest update = new ProductRequest(
                "Green Apple",
                "Organic and crisp",
                new BigDecimal("5.49"),
                null,
                8,
                null,
                category.getId(),
                "https://example.com/green-apple.jpg",
                shopId
        );

        ProductResponse updated = productService.updateProduct(created.id(), update);
        assertEquals("Green Apple", updated.name());
        assertEquals(8, updated.quantity());

        productService.deleteProduct(updated.id());
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(updated.id()));
    }
}
