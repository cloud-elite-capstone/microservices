package com.cartesian.agentservice.adapter.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.cartesian.agentservice.dto.ProductDto;

import reactor.core.publisher.Mono;

class WebSearchAdapterTest {

    private static final String TEST_API_KEY = "test-serpapi-key-123";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should return empty list when query is null, empty, or blank without invoking WebClient")
    void search_whenQueryIsNullOrBlank_returnsEmptyList(String query) {
        AtomicReference<Boolean> webClientCalled = new AtomicReference<>(false);
        ExchangeFunction exchangeFunction = request -> {
            webClientCalled.set(true);
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        WebSearchAdapter adapter = new WebSearchAdapter(webClient, TEST_API_KEY);

        List<ProductDto> results = adapter.search(query);

        assertThat(results).isNotNull().isEmpty();
        assertThat(webClientCalled.get()).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should return empty list when apiKey is null, empty, or blank without invoking WebClient")
    void search_whenApiKeyIsNullOrBlank_returnsEmptyList(String apiKey) {
        AtomicReference<Boolean> webClientCalled = new AtomicReference<>(false);
        ExchangeFunction exchangeFunction = request -> {
            webClientCalled.set(true);
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        WebSearchAdapter adapter = new WebSearchAdapter(webClient, apiKey);

        List<ProductDto> results = adapter.search("running shoes");

        assertThat(results).isNotNull().isEmpty();
        assertThat(webClientCalled.get()).isFalse();
    }

    @Test
    @DisplayName("Should return mapped ProductDto list and pass correct query parameters on shopping results")
    void search_whenShoppingResultsPresent_returnsMappedProductDtos() {
        String jsonResponse = """
                {
                    "shopping_results": [
                        {
                            "title": "Running Shoes Red",
                            "snippet": "Lightweight running shoes",
                            "price": "$99.99",
                            "thumbnail": "https://example.com/shoes-red.jpg"
                        },
                        {
                            "title": "Running Shoes Blue",
                            "snippet": "Durable running shoes",
                            "price": "$129.50",
                            "thumbnail": "https://example.com/shoes-blue.jpg"
                        }
                    ]
                }
                """;

        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(jsonResponse)
                    .build());
        };

        WebClient webClient = WebClient.builder().baseUrl("https://serpapi.com").exchangeFunction(exchangeFunction).build();
        WebSearchAdapter adapter = new WebSearchAdapter(webClient, TEST_API_KEY);

        List<ProductDto> results = adapter.search("running shoes");

        assertThat(results).isNotNull().hasSize(2);

        ProductDto first = results.get(0);
        assertThat(first.getId()).isNotNull();
        assertThat(first.getName()).isEqualTo("Running Shoes Red");
        assertThat(first.getDescription()).isEqualTo("Lightweight running shoes");
        assertThat(first.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(first.getImageUrl()).isEqualTo("https://example.com/shoes-red.jpg");
        assertThat(first.getQuantity()).isEqualTo(0);

        ProductDto second = results.get(1);
        assertThat(second.getId()).isNotNull();
        assertThat(second.getName()).isEqualTo("Running Shoes Blue");
        assertThat(second.getDescription()).isEqualTo("Durable running shoes");
        assertThat(second.getPrice()).isEqualByComparingTo(new BigDecimal("129.50"));
        assertThat(second.getImageUrl()).isEqualTo("https://example.com/shoes-blue.jpg");
        assertThat(second.getQuantity()).isEqualTo(0);

        assertThat(capturedRequest.get()).isNotNull();
        URI uri = capturedRequest.get().url();
        assertThat(uri.getPath()).isEqualTo("/search.json");
        assertThat(uri.getQuery()).contains("q=running shoes");
        assertThat(uri.getQuery()).contains("engine=google_shopping");
        assertThat(uri.getQuery()).contains("api_key=" + TEST_API_KEY);
    }

    @Test
    @DisplayName("Should return an empty list when API returns no results")
    void search_whenShoppingResultsEmpty_returnsEmptyList() {
        String jsonResponse = """
                {
                    "shopping_results": [],
                }
                """;

        ExchangeFunction exchangeFunction = request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(jsonResponse)
                        .build()
        );

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        WebSearchAdapter adapter = new WebSearchAdapter(webClient, TEST_API_KEY);

        List<ProductDto> results = adapter.search("mechanical keyboard");

        assertThat(results).isNotNull().hasSize(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "Free", "Contact for price", "N/A", "$1.2.3.4", "kapitan tutan"})
    @DisplayName("Should parse invalid or unparseable price strings to BigDecimal.ZERO")
    void search_whenPriceIsInvalidOrNonNumeric_parsesToZero(String rawPrice) {
        String jsonResponse = """
                {
                    "shopping_results": [
                        {
                            "title": "Item with special price",
                            "snippet": "Snippet",
                            "price": "%s",
                            "thumbnail": "https://example.com/item.jpg"
                        }
                    ]
                }
                """.formatted(rawPrice);

        ExchangeFunction exchangeFunction = request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(jsonResponse)
                        .build()
        );

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        WebSearchAdapter adapter = new WebSearchAdapter(webClient, TEST_API_KEY);

        List<ProductDto> results = adapter.search("query");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should handle null price in item and parse to BigDecimal.ZERO")
    void search_whenPriceIsNull_parsesToZero() {
        String jsonResponse = """
                {
                    "shopping_results": [
                        {
                            "title": "Item with null price",
                            "snippet": "Snippet",
                            "price": null,
                            "thumbnail": "https://example.com/item.jpg"
                        }
                    ]
                }
                """;

        ExchangeFunction exchangeFunction = request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(jsonResponse)
                        .build()
        );

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        WebSearchAdapter adapter = new WebSearchAdapter(webClient, TEST_API_KEY);

        List<ProductDto> results = adapter.search("query");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should parse price with currency symbols and commas correctly")
    void search_whenPriceHasCurrencySymbolsAndCommas_parsesCorrectly() {
        String jsonResponse = """
                {
                    "shopping_results": [
                        {
                            "title": "Expensive Laptop",
                            "snippet": "Gaming Laptop",
                            "price": "$1,499.99",
                            "thumbnail": "https://example.com/laptop.jpg"
                        }
                    ]
                }
                """;

        ExchangeFunction exchangeFunction = request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(jsonResponse)
                        .build()
        );

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        WebSearchAdapter adapter = new WebSearchAdapter(webClient, TEST_API_KEY);

        List<ProductDto> results = adapter.search("laptop");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPrice()).isEqualByComparingTo(new BigDecimal("1499.99"));
    }
}
