package com.cartesian.agentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public class ConversationSummaryDto {

    @JsonProperty("session_id")
    private UUID sessionId;

    @JsonProperty("user_id")
    private UUID userId;

    private String preview;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public ConversationSummaryDto() {
    }

    public ConversationSummaryDto(UUID sessionId, UUID userId, String preview, Instant createdAt, Instant updatedAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.preview = preview;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
