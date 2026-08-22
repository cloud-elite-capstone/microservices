package com.cartesian.agentservice.context;

import com.cartesian.agentservice.dto.ProductDto;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;

/**
 * Request-scoped context that holds products discovered during an AI chat turn.
 * Automatically created per HTTP request and garbage collected when the request completes.
 */
@Component
@RequestScope
public class ChatTurnContext {

    private final List<ProductDto> retrievedProducts = new ArrayList<>();

    public void addProduct(ProductDto product) {
        if (product != null) {
            this.retrievedProducts.add(product);
        }
    }

    public List<ProductDto> getRetrievedProducts() {
        return List.copyOf(retrievedProducts);
    }

    public void clear() {
        this.retrievedProducts.clear();
    }
}
