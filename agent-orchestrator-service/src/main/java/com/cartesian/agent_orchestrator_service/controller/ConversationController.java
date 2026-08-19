package com.cartesian.agent_orchestrator_service.controller;

import com.cartesian.agent_orchestrator_service.dto.agent.ChatRequest;
import com.cartesian.agent_orchestrator_service.dto.agent.ChatResponse;
import com.cartesian.agent_orchestrator_service.dto.agent.ConversationDto;
import com.cartesian.agent_orchestrator_service.dto.agent.ConversationSummaryDto;
import com.cartesian.agent_orchestrator_service.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agent/chat")
public class ConversationController {
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(conversationService.chat(request));
    }

    @GetMapping
    public ResponseEntity<List<ConversationSummaryDto>> listChatSessions(@RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(conversationService.listChatSessions(userId));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ConversationDto> getChatHistory(@PathVariable UUID sessionId) {
        return conversationService.getChatHistory(sessionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping( "/{sessionId}")
    public ResponseEntity<Void> deleteChat(@PathVariable UUID sessionId) {
        boolean deleted = conversationService.deleteChat(sessionId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
