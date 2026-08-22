package com.cartesian.agentservice.service;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.cartesian.agentservice.context.ChatTurnContext;
import com.cartesian.agentservice.dto.ChatMessageDto;
import com.cartesian.agentservice.dto.ChatRequest;
import com.cartesian.agentservice.dto.ChatResponse;
import com.cartesian.agentservice.dto.ConversationDto;
import com.cartesian.agentservice.dto.ConversationSummaryDto;
import com.cartesian.agentservice.dto.ProductDto;
import com.cartesian.agentservice.model.Conversation;
import com.cartesian.agentservice.repository.ConversationRepository;
import com.cartesian.agentservice.tools.AgentTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AgentChatService {

    private static final Logger log = LoggerFactory.getLogger(AgentChatService.class);

    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are Cartesian, an expert AI shopping assistant.
            Use the available tools to search for and retrieve products from the catalog.
            When recommending multiple products, reference them as [1], [2], [3] in the order they appear.
            Focus on matching the user's budget, location, and style preferences.
            Be concise, friendly, and conversational. Use markdown formatting.
            """;

    private final ChatClient chatClient;
    private final ChatTurnContext turnContext;
    private final ConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;

    public AgentChatService(
            ChatClient.Builder builder,
            AgentTools localTools,
            ChatTurnContext turnContext,
            ConversationRepository conversationRepository,
            ObjectMapper objectMapper,
            ObjectProvider<ToolCallbackProvider> mcpToolCallbackProvider
    ) {
        this.turnContext = turnContext;
        this.conversationRepository = conversationRepository;
        this.objectMapper = objectMapper;

        ChatClient.Builder clientBuilder = builder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultTools(localTools);

        mcpToolCallbackProvider.ifAvailable(provider ->
                clientBuilder.defaultTools(spec -> spec.callbacks(provider))
        );

        this.chatClient = clientBuilder.build();
    }

    public ChatResponse chat(ChatRequest request) {
        Conversation conversation = (request.getSessionId() != null)
                ? conversationRepository.findById(request.getSessionId()).orElseGet(Conversation::new)
                : new Conversation();

        List<ChatMessageDto> historyDtos = deserializeHistory(conversation.getHistory());

        String effectiveSystem = (request.getSystemInstruction() != null && !request.getSystemInstruction().isBlank())
                ? request.getSystemInstruction()
                : conversation.getSystemInstruction();

        List<Message> messages = new ArrayList<>();
        for (ChatMessageDto msg : historyDtos) {
            if ("user".equalsIgnoreCase(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equalsIgnoreCase(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        messages.add(new UserMessage(request.getMessage()));

        String reply;
        List<ProductDto> referencedProducts;

        try {
            turnContext.clear();

            var promptSpec = chatClient.prompt().messages(messages);

            if (effectiveSystem != null && !effectiveSystem.isBlank()) {
                promptSpec.system(effectiveSystem);
            }

            reply = promptSpec.call().content();
            referencedProducts = new ArrayList<>(turnContext.getRetrievedProducts());
        } catch (Exception e) {
            log.error("Vertex AI/LLM call failed: {}", e.getMessage(), e);
            reply = "I encountered an issue processing your request. Please try again.";
            referencedProducts = List.of();
        } finally {
            turnContext.clear();
        }

        // Persist conversation
        if (request.getUserId() != null) {
            conversation.setUserId(request.getUserId());
        }
        historyDtos.add(new ChatMessageDto("user", request.getMessage()));
        historyDtos.add(new ChatMessageDto("assistant", reply));
        conversation.setHistory(serializeHistory(historyDtos));
        if (effectiveSystem != null && !effectiveSystem.isBlank()) {
            conversation.setSystemInstruction(effectiveSystem);
        }
        conversationRepository.save(conversation);

        return new ChatResponse(conversation.getId(), reply, referencedProducts);
    }

    public List<ConversationSummaryDto> getUserConversations(UUID userId) {
        List<Conversation> convos = (userId != null)
                ? conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                : conversationRepository.findAllByOrderByUpdatedAtDesc();

        return convos.stream().map(c -> {
            List<ChatMessageDto> messages = deserializeHistory(c.getHistory());
            String preview = messages.isEmpty() ? "New Conversation" : messages.get(0).getContent();
            return new ConversationSummaryDto(
                    c.getId(),
                    c.getUserId(),
                    preview,
                    c.getCreatedAt(),
                    c.getUpdatedAt()
            );
        }).toList();
    }

    public Optional<ConversationDto> getConversation(UUID sessionId) {
        if (sessionId == null) return Optional.empty();
        return conversationRepository.findById(sessionId)
                .map(convo -> new ConversationDto(
                        convo.getId(),
                        convo.getUserId(),
                        deserializeHistory(convo.getHistory()),
                        convo.getSystemInstruction(),
                        convo.getCreatedAt(),
                        convo.getUpdatedAt()
                ));
    }

    public boolean deleteConversation(UUID sessionId) {
        if (sessionId != null && conversationRepository.existsById(sessionId)) {
            conversationRepository.deleteById(sessionId);
            return true;
        }
        return false;
    }

    private List<ChatMessageDto> deserializeHistory(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize conversation history: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String serializeHistory(List<ChatMessageDto> history) {
        try {
            return objectMapper.writeValueAsString(history);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize conversation history: {}", e.getMessage());
            return "[]";
        }
    }
}