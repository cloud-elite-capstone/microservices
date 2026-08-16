package com.retasify.shopservice.service;

import com.retasify.shopservice.dto.ShopDto;
import com.retasify.shopservice.dto.ShopRequest;
import com.retasify.shopservice.exception.InvalidLocationException;
import com.retasify.shopservice.exception.ShopNotFoundException;
import com.retasify.shopservice.model.Shop;
import com.retasify.shopservice.repository.ShopRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final ShopRepository shopRepository;

    public ShopService(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Transactional(readOnly = true)
    public List<ShopDto> getShopsByLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new InvalidLocationException("Location is required");
        }
        String[] parts = location.split(",");
        if (parts.length != 2) {
            throw new InvalidLocationException("Location must be in 'latitude,longitude' format");
        }
        try {
            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());
            Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
            return shopRepository.findByLocationIsNotNull().stream()
                .filter(shop -> shop.getLocation() != null)
                .filter(shop -> shop.getLocation().distance(point) <= 5000.0)
                .map(ShopDto::fromEntity)
                .collect(Collectors.toList());
        } catch (NumberFormatException ex) {
            throw new InvalidLocationException("Location must be numeric coordinates in 'latitude,longitude' format");
        }
    }

    @Transactional(readOnly = true)
    public ShopDto getShopById(UUID id) {
        Shop shop = shopRepository.findById(id)
            .orElseThrow(() -> new ShopNotFoundException(id));
        return ShopDto.fromEntity(shop);
    }

    @Transactional
    public ShopDto createShop(ShopRequest request) {
        validateRequest(request);
        Shop shop = new Shop();
        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setSellerId(request.getSellerId());
        shop.setLocation(request.getLocation());
        Shop saved = shopRepository.save(shop);
        return ShopDto.fromEntity(saved);
    }

    @Transactional
    public ShopDto updateShop(UUID id, ShopRequest request) {
        validateRequest(request);
        Shop shop = shopRepository.findById(id)
            .orElseThrow(() -> new ShopNotFoundException(id));
        shop.setName(request.getName());
        shop.setDescription(request.getDescription());
        shop.setLocation(request.getLocation());
        Shop updated = shopRepository.save(shop);
        return ShopDto.fromEntity(updated);
    }

    @Transactional
    public void deleteShop(UUID id) {
        if (!shopRepository.existsById(id)) {
            throw new ShopNotFoundException(id);
        }
        shopRepository.deleteById(id);
    }

    private void validateRequest(ShopRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Shop name is required");
        }
        if (request.getSellerId() == null) {
            throw new IllegalArgumentException("Seller ID is required");
        }
        if (request.getLocation() == null) {
            throw new InvalidLocationException("Shop location is required");
        }
    }
}
