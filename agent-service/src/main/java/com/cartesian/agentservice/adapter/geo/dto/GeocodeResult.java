package com.cartesian.agentservice.adapter.geo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodeResult(
        @JsonProperty("boundingbox") List<Double> boundingBox
) {}
