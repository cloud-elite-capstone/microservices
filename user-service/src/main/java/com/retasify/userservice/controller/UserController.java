package com.retasify.userservice.controller;

import com.retasify.userservice.dto.UserDto;
import com.retasify.userservice.dto.UserRegistrationRequest;
import com.retasify.userservice.service.UserService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/buyer/{id}")
    public ResponseEntity<UserDto> getBuyer(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getBuyer(id));
    }

    @GetMapping("/seller/{id}")
    public ResponseEntity<UserDto> getSeller(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getSeller(id));
    }

    @PostMapping("/buyers")
    public ResponseEntity<UserDto> createBuyer(@RequestBody UserRegistrationRequest request) {
        UserDto created = userService.createBuyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/sellers")
    public ResponseEntity<UserDto> createSeller(@RequestBody UserRegistrationRequest request) {
        UserDto created = userService.createSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/buyers/{id}")
    public ResponseEntity<UserDto> updateBuyer(@PathVariable UUID id, @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(userService.updateBuyer(id, request));
    }

    @PutMapping("/sellers/{id}")
    public ResponseEntity<UserDto> updateSeller(@PathVariable UUID id, @RequestBody UserRegistrationRequest request) {
        return ResponseEntity.ok(userService.updateSeller(id, request));
    }

    @DeleteMapping("/buyers/{id}")
    public ResponseEntity<Void> deleteBuyer(@PathVariable UUID id) {
        userService.deleteBuyer(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sellers/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable UUID id) {
        userService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }
}
