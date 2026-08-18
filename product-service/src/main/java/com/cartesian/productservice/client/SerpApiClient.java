package com.cartesian.productservice.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.cartesian.productservice.client.dto.serpapi.SerpApiResponse;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.cartesian.productservice.model.Product;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = "application/json")
public interface SerpApiClient {
    @GetExchange("/search.json")
    SerpApiResponse search(
            @RequestParam("q") String query,
            @RequestParam(name = "engine", defaultValue = "google_shopping") String engine,
            @RequestParam("api_key") String apiKey
    );
}
