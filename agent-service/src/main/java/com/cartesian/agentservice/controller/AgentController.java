package com.cartesian.agentservice.controller;

import com.cartesian.agentservice.dto.*;
import com.cartesian.agentservice.service.AgentChatService;
import com.cartesian.agentservice.service.AgentService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
public class AgentController {
    private final AgentService agentService;
    private final AgentChatService agentChatService;

    public AgentController(
            AgentService agentService,
            Optional<AgentChatService> agentChatService
    ) {
        this.agentService = agentService;
        this.agentChatService = agentChatService.orElse(null);
    }

    @PostMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(@RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(agentService.generateRecommendations(request));
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (agentChatService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ChatResponse(
                    request.getSessionId(),
                    "Chat is not available. Set the GCP_PROJECT_ID environment variable and ensure Application Default Credentials are configured.",
                    List.of()));
        }
        return ResponseEntity.ok(agentChatService.chat(request));
    }

    @GetMapping("/chat")
    public ResponseEntity<List<ConversationSummaryDto>> listChatSessions(@RequestParam(required = false) UUID userId) {
        if (agentChatService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(List.of());
//                    .body(Map.of("message", "Chat is not available. Set the GCP_PROJECT_ID environment variable and ensure Application Default Credentials are configured."));
        }
        return ResponseEntity.ok(agentChatService.getUserConversations(userId));
    }

    @GetMapping("/chat/{sessionId}")
    public ResponseEntity<ConversationDto> getChatHistory(@PathVariable UUID sessionId) {
        if (agentChatService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ConversationDto());
//                    .body(Map.of("message", "Chat is not available. Set the GCP_PROJECT_ID environment variable and ensure Application Default Credentials are configured."));
        }
        return agentChatService.getConversation(sessionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/chat/{sessionId}")
    public ResponseEntity<Void> deleteChat(@PathVariable UUID sessionId) {
        if (agentChatService == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
//                    .body(Map.of("message", "Chat is not available. Set the GCP_PROJECT_ID environment variable and ensure Application Default Credentials are configured."));
        }
        boolean deleted = agentChatService.deleteConversation(sessionId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
