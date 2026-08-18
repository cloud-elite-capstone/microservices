package com.cartesian.productservice.client;

import org.springframework.web.bind.annotation.RequestBody;

import com.cartesian.productservice.client.dto.agent.AgentSearchRequest;
import com.cartesian.productservice.client.dto.agent.AgentSearchResponse;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange(accept = "application/json", contentType = "application/json")
public interface AgentClient {
    @PostExchange("/agent/recommendations")
    AgentSearchResponse recommend(@RequestBody AgentSearchRequest request);
}