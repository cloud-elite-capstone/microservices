package com.cartesian.productservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import com.cartesian.productservice.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cartesian.productservice.exception.ProductNotFoundException;
import com.cartesian.productservice.model.Category;
import com.cartesian.productservice.repository.CategoryRepository;
import com.cartesian.productservice.repository.ProductRepository;
import com.cartesian.productservice.service.ProductService;

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
        UUID id = UUID.randomUUID();

        ProductDto request = new ProductDto(
                id,
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

        ProductDto created = productService.createProduct(request);
        assertNotNull(created.id());
        assertEquals("Apple", created.name());

        ProductDto fetched = productService.getProductById(created.id());
        assertEquals("Crisp and sweet", fetched.description());

        ProductDto update = new ProductDto(
                id,
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

        ProductDto updated = productService.updateProduct(created.id(), update);
        assertEquals("Green Apple", updated.name());
        assertEquals(8, updated.quantity());

        productService.deleteProduct(updated.id());
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(updated.id()));
    }
}
