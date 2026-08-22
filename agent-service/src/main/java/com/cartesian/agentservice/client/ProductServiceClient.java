package com.cartesian.agentservice.client;

import com.cartesian.agentservice.dto.ProductDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@HttpExchange("/products")
public interface ProductServiceClient {
    @GetExchange("/{id}")
    Optional<ProductDto> getProductById(@PathVariable UUID id);

    @PostExchange(value = "/search", contentType = MediaType.TEXT_PLAIN_VALUE)
    List<ProductDto> searchLocalProducts(@RequestBody String query);
}
