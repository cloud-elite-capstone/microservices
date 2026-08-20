package com.cartesian.agent_orchestrator_service.config.client;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;

import reactor.core.publisher.Mono;

public class IdTokenExchangeFilter implements ExchangeFilterFunction {
    private final IdTokenCredentials idTokenCredentials;

    public IdTokenExchangeFilter(String audience, boolean enabled) throws IOException {
        if (!enabled) {
            this.idTokenCredentials = null;
            return;
        }
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        if (!(credentials instanceof IdTokenProvider provider)) {
            throw new IllegalStateException(
                    "Application default credentials do not support ID tokens. "
                            + "Ensure the runtime service account is used and iamcredentials.googleapis.com is enabled.");
        }
        this.idTokenCredentials = IdTokenCredentials.newBuilder()
                .setIdTokenProvider(provider)
                .setTargetAudience(audience)
                .build();
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (idTokenCredentials == null) {
            return next.exchange(request);
        }
        try {
            idTokenCredentials.refreshIfExpired();
            String token = idTokenCredentials.getAccessToken().getTokenValue();
            ClientRequest authorizedRequest = ClientRequest.from(request)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
            return next.exchange(authorizedRequest);
        } catch (IOException e) {
            return Mono.error(e);
        }
    }
}
