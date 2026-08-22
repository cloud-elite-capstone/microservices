package com.cartesian.agentservice.controller;

import com.cartesian.agentservice.dto.*;
import com.cartesian.agentservice.service.AgentChatService;
import com.cartesian.agentservice.service.AgentService;

import java.util.List;
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
            AgentChatService agentChatService
    ) {
        this.agentService = agentService;
        this.agentChatService = agentChatService;
    }

    @PostMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(@RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(agentService.generateRecommendations(request));
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(agentChatService.chat(request));
    }

    @GetMapping("/chat")
    public ResponseEntity<List<ConversationSummaryDto>> listChatSessions(@RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(agentChatService.getUserConversations(userId));
    }

    @GetMapping("/chat/{sessionId}")
    public ResponseEntity<ConversationDto> getChatHistory(@PathVariable UUID sessionId) {
        return agentChatService.getConversation(sessionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/chat/{sessionId}")
    public ResponseEntity<Void> deleteChat(@PathVariable UUID sessionId) {
        if (agentChatService.deleteConversation(sessionId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
