package com.cartesian.agent_orchestrator_service.config.client;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.cartesian.agent_orchestrator_service.client.AgentServiceClient;

@Configuration
public class AgentServiceClientConfig {
    @Bean
    public AgentServiceClient agentClient(
            @Value("${microservices.agent.url}") String baseUrl,
            @Value("${microservices.auth.id-token.enabled:false}") boolean idTokenEnabled) throws IOException {
        WebClient restClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(new IdTokenExchangeFilter(baseUrl, idTokenEnabled))
                .build();

        WebClientAdapter adapter = WebClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AgentServiceClient.class);
    }
}
