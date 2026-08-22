package com.cartesian.productservice.service;

import java.util.List;
import java.util.UUID;

import com.cartesian.productservice.dto.ProductDto;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cartesian.productservice.exception.CategoryNotFoundException;
import com.cartesian.productservice.exception.ProductNotFoundException;
import com.cartesian.productservice.mapper.ProductMapper;
import com.cartesian.productservice.model.Category;
import com.cartesian.productservice.model.Product;
import com.cartesian.productservice.repository.CategoryRepository;
import com.cartesian.productservice.repository.ProductRepository;

@Service
public class ProductService {
//    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    private Product findProductByIdOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Category findCategoryByIdOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID id) {
        Product product = findProductByIdOrThrow(id);
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto request) {
        findCategoryByIdOrThrow(request.categoryId());

        Product product = productMapper.toEntity(request);

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(UUID id, ProductDto request) {
        Product product = findProductByIdOrThrow(id);
        findCategoryByIdOrThrow(request.categoryId());

        productMapper.updateEntityFromRequest(request, product);

        Product updated = productRepository.save(product);
        return productMapper.toDto(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        findProductByIdOrThrow(id);
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchLocalProducts(String query) {
        return productRepository.searchByNameOrDescription(query).stream()
                .map(productMapper::toDto)
                .toList();
    }
}
