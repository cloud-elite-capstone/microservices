package com.cartesian.agent_orchestrator_service.client;

import com.cartesian.agent_orchestrator_service.dto.agent.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@HttpExchange("/agent")
public interface AgentServiceClient {
    @PostExchange("/recommendations")
    RecommendationResponse getRecommendations(@RequestBody RecommendationRequest request);

    @PostExchange("/chat")
    ChatResponse chat(@RequestBody ChatRequest request);

    @GetExchange("/chat")
    List<ConversationSummaryDto> listChatSessions(@RequestParam(required = false) UUID userId);

    @GetExchange("/chat/{sessionId}")
    Optional<ConversationDto> getChatHistory(@PathVariable UUID sessionId);

    @DeleteExchange("/chat/{sessionId}")
    boolean deleteChat(@PathVariable UUID sessionId);
}
