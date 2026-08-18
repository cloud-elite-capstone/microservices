package com.cartesian.agentservice.repository;

import com.cartesian.agentservice.model.Conversation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    List<Conversation> findAllByOrderByUpdatedAtDesc();
}
