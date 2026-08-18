package com.cartesian.productservice.client.dto.geocoder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodeResult(
        @JsonProperty("boundingbox") List<Double> boundingBox
) {}
