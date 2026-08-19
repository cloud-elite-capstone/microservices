package com.cartesian.agent_orchestrator_service.dto.search;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductSearchRequest(
        @Size(max = 100, message = "Search term must not exceed 100 characters")
        String search,

        @Size(max = 100, message = "Location must not exceed 100 characters")
        String location,

        @PositiveOrZero(message = "Minimum budget must be zero or positive")
        BigDecimal minBudget,

        @PositiveOrZero(message = "Maximum budget must be zero or positive")
        BigDecimal maxBudget,

        @DecimalMin(value = "0.0", message = "Minimum rating cannot be less than 0.0")
        @DecimalMax(value = "5.0", message = "Minimum rating cannot exceed 5.0")
        Double minRating,

        @DecimalMin(value = "0.0", message = "Maximum rating cannot be less than 0.0")
        @DecimalMax(value = "5.0", message = "Maximum rating cannot exceed 5.0")
        Double maxRating,

        UUID sourceShop,

        MultipartFile image,

        @Size(max = 2048, message = "Image URL is too long")
        String imageUrl
) {
    @AssertTrue(message = "minBudget must be less than or equal to maxBudget")
    public boolean isBudgetRangeValid() {
        if (minBudget == null || maxBudget == null) {
            return true;
        }
        return minBudget.compareTo(maxBudget) <= 0;
    }

    @AssertTrue(message = "minRating must be less than or equal to maxRating")
    public boolean isRatingRangeValid() {
        if (minRating == null || maxRating == null) {
            return true;
        }
        return minRating <= maxRating;
    }

    @AssertTrue(message = "Cannot provide both an image file and an image URL")
    public boolean isImageSourceValid() {
        boolean hasFile = image != null && !image.isEmpty();
        boolean hasUrl = imageUrl != null && !imageUrl.isBlank();

        return !(hasFile && hasUrl);
    }
}
