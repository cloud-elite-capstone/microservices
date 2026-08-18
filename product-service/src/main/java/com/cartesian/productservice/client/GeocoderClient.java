package com.cartesian.productservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange(accept = "application/json", headers = "User-Agent=cartesian-product-service")
public interface GeocoderClient {
    @GetExchange("/search")
    List<GeocodeResult> search(
            @RequestParam("q") String location,
            @RequestParam(name = "format", defaultValue = "jsonv2") String format,
            @RequestParam(name = "limit", defaultValue = "1") int limit
    );

    default List<GeocodeResult> search(String location) {
        return search(location, "jsonv2", 1);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GeocodeResult(
            @JsonProperty("boundingbox") List<Double> boundingBox
    ) {}
}