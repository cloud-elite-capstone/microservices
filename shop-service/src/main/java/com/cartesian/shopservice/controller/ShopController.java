package com.cartesian.shopservice.controller;

import com.cartesian.shopservice.dto.ShopDto;
import com.cartesian.shopservice.dto.ShopRequest;
import com.cartesian.shopservice.service.ShopService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping
    public ResponseEntity<List<ShopDto>> getShopsByLocation(@RequestParam("location") String location) {
        return ResponseEntity.ok(shopService.getShopsByLocation(location));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopDto> getShopById(@PathVariable UUID id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @PostMapping
    public ResponseEntity<ShopDto> createShop(@RequestBody ShopRequest request) {
        ShopDto created = shopService.createShop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopDto> updateShop(@PathVariable UUID id, @RequestBody ShopRequest request) {
        return ResponseEntity.ok(shopService.updateShop(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable UUID id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}
