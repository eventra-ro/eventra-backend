package com.eventra.dto.response;

import com.eventra.entity.enums.SubscriptionPlan;
import com.eventra.entity.enums.VendorStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VendorProfileResponse(
        UUID id,
        UUID userId,
        String slug,
        String businessName,
        String description,
        String phone,
        String websiteUrl,
        String instagramUrl,
        String facebookUrl,
        BigDecimal priceFrom,
        BigDecimal priceTo,
        String priceCurrency,
        VendorStatus status,
        SubscriptionPlan subscriptionPlan,
        BigDecimal averageRating,
        int reviewCount,
        int viewCount,
        int contactCount,
        boolean isFeatured,
        List<CategoryInfo> categories,
        List<CountyInfo> counties,
        List<EventTypeInfo> eventTypes,
        List<PhotoInfo> photos,
        Instant createdAt
) {
    public record CategoryInfo(UUID id, String code, String name) {}
    public record CountyInfo(UUID id, String code, String name) {}
    public record EventTypeInfo(UUID id, String code, String name, String icon) {}
    public record PhotoInfo(UUID id, String url, String thumbnailUrl,
                            boolean isCover, int sortOrder) {}
}