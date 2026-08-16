package com.retasify.productservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.retasify.productservice.dto.ProductDto;
import com.retasify.productservice.exception.ProductNotFoundException;
import com.retasify.productservice.model.Category;
import com.retasify.productservice.repository.CategoryRepository;
import com.retasify.productservice.repository.ProductRepository;
import com.retasify.productservice.service.ProductService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

        ProductDto request = new ProductDto();
        request.setName("Apple");
        request.setDescription("Crisp and sweet");
        request.setPrice(new BigDecimal("4.99"));
        request.setQuantity(12);
        request.setCategoryId(category.getId());
        request.setShopId(UUID.randomUUID());
        request.setImageUrl("https://example.com/apple.jpg");

        ProductDto created = productService.createProduct(request);
        assertNotNull(created.getId());
        assertEquals("Apple", created.getName());

        ProductDto fetched = productService.getProductById(created.getId());
        assertEquals("Crisp and sweet", fetched.getDescription());

        ProductDto update = new ProductDto();
        update.setName("Green Apple");
        update.setDescription("Organic and crisp");
        update.setPrice(new BigDecimal("5.49"));
        update.setQuantity(8);
        update.setCategoryId(category.getId());
        update.setShopId(request.getShopId());
        update.setImageUrl("https://example.com/green-apple.jpg");

        ProductDto updated = productService.updateProduct(created.getId(), update);
        assertEquals("Green Apple", updated.getName());
        assertEquals(8, updated.getQuantity());

        productService.deleteProduct(updated.getId());
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(updated.getId()));
    }
}
