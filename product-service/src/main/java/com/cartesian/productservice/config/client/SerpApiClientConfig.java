package com.cartesian.productservice.config.client;

import com.cartesian.productservice.client.SerpApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class SerpApiClientConfig {
    @Bean
    public SerpApiClient serpApiClient(
            RestClient.Builder builder,
            @Value("${client.serpapi.url}") String baseUrl
    ) {
        RestClient restClient = builder
                .baseUrl(baseUrl)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(SerpApiClient.class);
    }
}
