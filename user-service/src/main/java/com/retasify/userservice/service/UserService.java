package com.retasify.userservice.service;

import com.retasify.userservice.dto.UserDto;
import com.retasify.userservice.dto.UserRegistrationRequest;
import com.retasify.userservice.exception.EmailAlreadyExistsException;
import com.retasify.userservice.exception.UserNotFoundException;
import com.retasify.userservice.model.Buyer;
import com.retasify.userservice.model.Seller;
import com.retasify.userservice.model.User;
import com.retasify.userservice.repository.BuyerRepository;
import com.retasify.userservice.repository.SellerRepository;
import com.retasify.userservice.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String VALID_SEX_MALE = "male";
    private static final String VALID_SEX_FEMALE = "female";

    private final UserRepository userRepository;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;

    public UserService(UserRepository userRepository, BuyerRepository buyerRepository, SellerRepository sellerRepository) {
        this.userRepository = userRepository;
        this.buyerRepository = buyerRepository;
        this.sellerRepository = sellerRepository;
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        return UserDto.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public UserDto getBuyer(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Buyer not found with id: " + id));
        if (!(user instanceof Buyer)) {
            throw new UserNotFoundException("Buyer not found with id: " + id);
        }
        return UserDto.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public UserDto getSeller(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Seller not found with id: " + id));
        if (!(user instanceof Seller)) {
            throw new UserNotFoundException("Seller not found with id: " + id);
        }
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto createBuyer(UserRegistrationRequest request) {
        validateRequest(request);
        checkEmailAvailability(request.getEmail());
        Buyer buyer = new Buyer();
        buyer.setUsername(request.getUsername());
        buyer.setEmail(request.getEmail());
        buyer.setPasswordHash(request.getPasswordHash());
        buyer.setSex(request.getSex());
        Buyer saved = buyerRepository.save(buyer);
        return UserDto.fromEntity(saved);
    }

    @Transactional
    public UserDto createSeller(UserRegistrationRequest request) {
        validateRequest(request);
        checkEmailAvailability(request.getEmail());
        Seller seller = new Seller();
        seller.setUsername(request.getUsername());
        seller.setEmail(request.getEmail());
        seller.setPasswordHash(request.getPasswordHash());
        seller.setSex(request.getSex());
        Seller saved = sellerRepository.save(seller);
        return UserDto.fromEntity(saved);
    }

    @Transactional
    public UserDto updateBuyer(UUID id, UserRegistrationRequest request) {
        Buyer buyer = buyerRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Buyer not found with id: " + id));
        updateUserFields(buyer, request);
        Buyer updated = buyerRepository.save(buyer);
        return UserDto.fromEntity(updated);
    }

    @Transactional
    public UserDto updateSeller(UUID id, UserRegistrationRequest request) {
        Seller seller = sellerRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("Seller not found with id: " + id));
        updateUserFields(seller, request);
        Seller updated = sellerRepository.save(seller);
        return UserDto.fromEntity(updated);
    }

    @Transactional
    public void deleteBuyer(UUID id) {
        if (!buyerRepository.existsById(id)) {
            throw new UserNotFoundException("Buyer not found with id: " + id);
        }
        buyerRepository.deleteById(id);
    }

    @Transactional
    public void deleteSeller(UUID id) {
        if (!sellerRepository.existsById(id)) {
            throw new UserNotFoundException("Seller not found with id: " + id);
        }
        sellerRepository.deleteById(id);
    }

    private void validateRequest(UserRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPasswordHash() == null || request.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }
        if (request.getSex() == null || request.getSex().isBlank()) {
            throw new IllegalArgumentException("Sex is required");
        }
        String normalizedSex = request.getSex().trim().toLowerCase();
        if (!VALID_SEX_MALE.equals(normalizedSex) && !VALID_SEX_FEMALE.equals(normalizedSex)) {
            throw new IllegalArgumentException("Sex must be either 'male' or 'female'");
        }
        request.setSex(normalizedSex);
    }

    private void checkEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    private void updateUserFields(User user, UserRegistrationRequest request) {
        validateRequest(request);
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPasswordHash());
        user.setSex(request.getSex());
    }
}
