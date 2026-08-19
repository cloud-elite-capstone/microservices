package com.cartesian.agent_orchestrator_service.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    @JsonProperty("session_id")
    private UUID sessionId;

    @JsonProperty("user_id")
    private UUID userId;

    private List<ChatMessageDto> messages;

    @JsonProperty("system_instruction")
    private String systemInstruction;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;
}
