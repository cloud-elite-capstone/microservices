package com.cartesian.productservice.client.dto.serpapi;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SerpApiItem(
        @JsonProperty("title")
        @JsonAlias("name")
        String title,

        @JsonProperty("snippet")
        String snippet,

        @JsonProperty("price")
        String rawPrice,

        @JsonProperty("thumbnail")
        @JsonAlias("image")
        String imageUrl
) {}
