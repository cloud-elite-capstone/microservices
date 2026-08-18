package com.cartesian.userservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cartesian.userservice.dto.UserDto;
import com.cartesian.userservice.dto.UserRegistrationRequest;
import com.cartesian.userservice.exception.UserNotFoundException;
import com.cartesian.userservice.repository.BuyerRepository;
import com.cartesian.userservice.repository.SellerRepository;
import com.cartesian.userservice.repository.UserRepository;
import com.cartesian.userservice.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceCrudTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @BeforeEach
    void setUp() {
        buyerRepository.deleteAll();
        sellerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void buyerCrudFlow() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("buyer1");
        request.setEmail("buyer1@example.com");
        request.setPasswordHash("hash1");
        request.setSex("male");

        UserDto created = userService.createBuyer(request);
        assertNotNull(created.getId());
        assertEquals("buyer1", created.getUsername());

        UserDto fetched = userService.getBuyer(created.getId());
        assertEquals("buyer1@example.com", fetched.getEmail());

        UserRegistrationRequest update = new UserRegistrationRequest();
        update.setUsername("buyer1-updated");
        update.setEmail("buyer1-updated@example.com");
        update.setPasswordHash("hash2");
        update.setSex("female");

        UserDto updated = userService.updateBuyer(created.getId(), update);
        assertEquals("buyer1-updated", updated.getUsername());
        assertEquals("female", updated.getSex());

        userService.deleteBuyer(updated.getId());
        assertThrows(UserNotFoundException.class, () -> userService.getBuyer(updated.getId()));
    }

    @Test
    void sellerCrudFlow() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("seller1");
        request.setEmail("seller1@example.com");
        request.setPasswordHash("hash1");
        request.setSex("female");

        UserDto created = userService.createSeller(request);
        assertNotNull(created.getId());
        assertEquals("seller1", created.getUsername());

        UserDto fetched = userService.getSeller(created.getId());
        assertEquals("seller1@example.com", fetched.getEmail());

        UserRegistrationRequest update = new UserRegistrationRequest();
        update.setUsername("seller1-updated");
        update.setEmail("seller1-updated@example.com");
        update.setPasswordHash("hash2");
        update.setSex("male");

        UserDto updated = userService.updateSeller(created.getId(), update);
        assertEquals("seller1-updated", updated.getUsername());
        assertEquals("male", updated.getSex());

        userService.deleteSeller(updated.getId());
        assertThrows(UserNotFoundException.class, () -> userService.getSeller(updated.getId()));
    }
}
