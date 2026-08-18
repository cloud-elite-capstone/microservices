package com.cartesian.agentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatResponse {

    @JsonProperty("session_id")
    private UUID sessionId;

    private String reply;

    @JsonProperty("referenced_products")
    private List<ProductDto> referencedProducts = new ArrayList<>();

    public ChatResponse() {
    }

    public ChatResponse(UUID sessionId, String reply, List<ProductDto> referencedProducts) {
        this.sessionId = sessionId;
        this.reply = reply;
        this.referencedProducts = referencedProducts;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<ProductDto> getReferencedProducts() {
        return referencedProducts;
    }

    public void setReferencedProducts(List<ProductDto> referencedProducts) {
        this.referencedProducts = referencedProducts;
    }
}
