package com.cartesian.productservice.controller;

import com.cartesian.productservice.service.ProductService;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController {

    private final ProductService productService;

    public AgentController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/agent/recommendations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> recommendations(@RequestBody Map<String, Object> requestBody) {
        return ResponseEntity.ok(productService.searchProducts(requestBody));
    }
}
