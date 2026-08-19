package com.cartesian.agent_orchestrator_service.service;

import com.cartesian.agent_orchestrator_service.client.AgentServiceClient;
import com.cartesian.agent_orchestrator_service.dto.agent.ChatRequest;
import com.cartesian.agent_orchestrator_service.dto.agent.ChatResponse;
import com.cartesian.agent_orchestrator_service.dto.agent.ConversationDto;
import com.cartesian.agent_orchestrator_service.dto.agent.ConversationSummaryDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {
    private final AgentServiceClient agentServiceClient;

    public ConversationService(AgentServiceClient agentServiceClient) {
        this.agentServiceClient = agentServiceClient;
    }

    public ChatResponse chat(ChatRequest request) {
        return agentServiceClient.chat(request);
    }

    public List<ConversationSummaryDto> listChatSessions(UUID userId) {
        return agentServiceClient.listChatSessions(userId);
    }

    public Optional<ConversationDto> getChatHistory(UUID sessionId) {
        return agentServiceClient.getChatHistory(sessionId);
    }

    public boolean deleteChat(UUID sessionId) {
        return agentServiceClient.deleteChat(sessionId);
    }
}
