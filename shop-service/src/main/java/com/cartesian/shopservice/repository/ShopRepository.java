package com.cartesian.shopservice.repository;

import com.cartesian.shopservice.model.Shop;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, UUID> {

    List<Shop> findByLocationIsNotNull();
}
