package com.cartesian.agent_orchestrator_service.adapter.geo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GeoAdapterTest {
    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        geometryFactory = new GeometryFactory();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should return null when location is null, empty, or blank without invoking WebClient")
    void geocodeToBoundingBoxPolygon_whenLocationIsNullOrBlank_returnsNull(String location) {
        AtomicReference<Boolean> webClientCalled = new AtomicReference<>(false);
        ExchangeFunction exchangeFunction = request -> {
            webClientCalled.set(true);
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        GeoAdapter geoAdapter = new GeoAdapter(webClient, geometryFactory);

        Polygon result = geoAdapter.geocodeToBoundingBoxPolygon(location);

        assertThat(result).isNull();
        assertThat(webClientCalled.get()).isFalse();
    }

    @Test
    @DisplayName("Should return valid Polygon bounding box and send correct query parameters on success")
    void geocodeToBoundingBoxPolygon_whenSuccess_returnsPolygonWithCorrectCoordinates() {
        String jsonResponse = """
                [
                    {
                        "place_id": 12345,
                        "licence": "Data © OpenStreetMap contributors",
                        "lat": "47.6038321",
                        "lon": "-122.330062",
                        "display_name": "Seattle, King County, Washington, USA",
                        "boundingbox": [47.4953154, 47.7341357, -122.436232, -122.2249728]
                    }
                ]
                """;

        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            capturedRequest.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(jsonResponse)
                    .build());
        };

        WebClient webClient = WebClient.builder().baseUrl("https://nominatim.openstreetmap.org").exchangeFunction(exchangeFunction).build();
        GeoAdapter geoAdapter = new GeoAdapter(webClient, geometryFactory);

        Polygon result = geoAdapter.geocodeToBoundingBoxPolygon("Seattle");

        assertThat(result).isNotNull();

        Envelope envelope = result.getEnvelopeInternal();
        assertThat(envelope.getMinY()).isEqualTo(47.4953154); // south
        assertThat(envelope.getMaxY()).isEqualTo(47.7341357); // north
        assertThat(envelope.getMinX()).isEqualTo(-122.436232); // west
        assertThat(envelope.getMaxX()).isEqualTo(-122.2249728); // east

        assertThat(capturedRequest.get()).isNotNull();
        URI uri = capturedRequest.get().url();
        assertThat(uri.getPath()).isEqualTo("/search");
        assertThat(uri.getQuery()).contains("q=Seattle");
        assertThat(uri.getQuery()).contains("format=jsonv2");
        assertThat(uri.getQuery()).contains("limit=1");
    }

    @Test
    @DisplayName("Should return null when geocoder returns empty list")
    void geocodeToBoundingBoxPolygon_whenEmptyResults_returnsNull() {
        ExchangeFunction exchangeFunction = request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("[]")
                        .build()
        );

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        GeoAdapter geoAdapter = new GeoAdapter(webClient, geometryFactory);

        Polygon result = geoAdapter.geocodeToBoundingBoxPolygon("NonExistentPlace12345");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when response body is null or 204 No Content")
    void geocodeToBoundingBoxPolygon_whenNoContentResponse_returnsNull() {
        ExchangeFunction exchangeFunction = request -> Mono.just(
                ClientResponse.create(HttpStatus.NO_CONTENT).build()
        );

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        GeoAdapter geoAdapter = new GeoAdapter(webClient, geometryFactory);

        Polygon result = geoAdapter.geocodeToBoundingBoxPolygon("Seattle");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when boundingbox is null")
    void geocodeToBoundingBoxPolygon_whenBoundingBoxIsNull_returnsNull() {
        String jsonResponse = """
                [
                    {
                        "place_id": 12345,
                        "display_name": "Seattle",
                        "boundingbox": null
                    }
                ]
                """;

        ExchangeFunction exchangeFunction = request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(jsonResponse)
                        .build()
        );

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        GeoAdapter geoAdapter = new GeoAdapter(webClient, geometryFactory);

        Polygon result = geoAdapter.geocodeToBoundingBoxPolygon("Seattle");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when boundingbox has fewer than 4 coordinates")
    void geocodeToBoundingBoxPolygon_whenBoundingBoxHasLessThanFourElements_returnsNull() {
        String jsonResponse = """
                [
                    {
                        "place_id": 12345,
                        "display_name": "Seattle",
                        "boundingbox": [47.4953154, 47.7341357]
                    }
                ]
                """;

        ExchangeFunction exchangeFunction = request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(jsonResponse)
                        .build()
        );

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        GeoAdapter geoAdapter = new GeoAdapter(webClient, geometryFactory);

        Polygon result = geoAdapter.geocodeToBoundingBoxPolygon("Seattle");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when geocoder returns HTTP 500 server error")
    void geocodeToBoundingBoxPolygon_whenHttpServerError_returnsNull() {
        ExchangeFunction exchangeFunction = request -> Mono.just(
                ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("{\"error\": \"Internal Server Error\"}")
                        .build()
        );

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        GeoAdapter geoAdapter = new GeoAdapter(webClient, geometryFactory);

        Polygon result = geoAdapter.geocodeToBoundingBoxPolygon("Seattle");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when WebClient throws an exception during exchange")
    void geocodeToBoundingBoxPolygon_whenExceptionThrown_returnsNull() {
        ExchangeFunction exchangeFunction = request -> Mono.error(new RuntimeException("Connection timeout"));

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        GeoAdapter geoAdapter = new GeoAdapter(webClient, geometryFactory);

        Polygon result = geoAdapter.geocodeToBoundingBoxPolygon("Seattle");

        assertThat(result).isNull();
    }
}
