package com.cartesian.agent_orchestrator_service.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @JsonProperty("session_id")
    private UUID sessionId;

    @JsonProperty("user_id")
    private UUID userId;

    private String message;

    @JsonProperty("system_instruction")
    private String systemInstruction;
}
