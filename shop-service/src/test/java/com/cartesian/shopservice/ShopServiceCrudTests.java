package com.cartesian.shopservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cartesian.shopservice.dto.ShopDto;
import com.cartesian.shopservice.dto.ShopRequest;
import com.cartesian.shopservice.exception.ShopNotFoundException;
import com.cartesian.shopservice.repository.ShopRepository;
import com.cartesian.shopservice.service.ShopService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShopServiceCrudTests {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ShopRepository shopRepository;

    @BeforeEach
    void setUp() {
        shopRepository.deleteAll();
    }

    @Test
    void shopCrudFlow() {
        GeometryFactory geometryFactory = new GeometryFactory();
        ShopRequest request = new ShopRequest();
        request.setName("Corner Mart");
        request.setDescription("Fresh groceries");
        request.setSellerId(UUID.randomUUID());
        request.setLocation(geometryFactory.createPoint(new Coordinate(-73.9857, 40.7484)));

        ShopDto created = shopService.createShop(request);
        assertNotNull(created.getId());
        assertEquals("Corner Mart", created.getName());

        ShopDto fetched = shopService.getShopById(created.getId());
        assertEquals("Fresh groceries", fetched.getDescription());

        ShopRequest update = new ShopRequest();
        update.setName("Corner Mart Updated");
        update.setDescription("Fresh groceries and bakery");
        update.setSellerId(request.getSellerId());
        update.setLocation(geometryFactory.createPoint(new Coordinate(-73.9900, 40.7420)));

        ShopDto updated = shopService.updateShop(created.getId(), update);
        assertEquals("Corner Mart Updated", updated.getName());

        shopService.deleteShop(updated.getId());
        assertThrows(ShopNotFoundException.class, () -> shopService.getShopById(updated.getId()));
    }
}
