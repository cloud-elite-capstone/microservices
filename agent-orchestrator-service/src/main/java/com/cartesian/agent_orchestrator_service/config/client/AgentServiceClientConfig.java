package com.cartesian.agent_orchestrator_service.config.client;

import com.cartesian.agent_orchestrator_service.client.AgentServiceClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class AgentServiceClientConfig {
    @Bean
    public AgentServiceClient agentClient(@Value("${microservices.agent.url}") String baseUrl) throws IOException {
        WebClient restClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(new IdTokenExchangeFilter(baseUrl))
                .build();

        WebClientAdapter adapter = WebClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AgentServiceClient.class);
    }
}
