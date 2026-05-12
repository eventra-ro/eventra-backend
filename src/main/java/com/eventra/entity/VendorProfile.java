package com.eventra.entity;

import com.eventra.entity.enums.SubscriptionPlan;
import com.eventra.entity.enums.VendorStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "vendor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "slug", length = 255, unique = true)
    private String slug;

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "instagram_url", length = 500)
    private String instagramUrl;

    @Column(name = "facebook_url", length = 500)
    private String facebookUrl;

    @Column(name = "price_from", precision = 10, scale = 2)
    private BigDecimal priceFrom;

    @Column(name = "price_to", precision = 10, scale = 2)
    private BigDecimal priceTo;

    @Column(name = "price_currency", nullable = false, length = 3)
    @Builder.Default
    private String priceCurrency = "RON";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "vendor_status")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Builder.Default
    private VendorStatus status = VendorStatus.PENDING_REVIEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false,
            columnDefinition = "subscription_plan")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    @Builder.Default
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    @Column(name = "subscription_ends_at")
    private Instant subscriptionEndsAt;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "review_count", nullable = false)
    @Builder.Default
    private int reviewCount = 0;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "contact_count", nullable = false)
    @Builder.Default
    private int contactCount = 0;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private boolean isFeatured = false;

    @Column(name = "featured_until")
    private Instant featuredUntil;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "vendor_category_assignments",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<VendorCategory> categories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "vendor_county_coverage",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "county_id")
    )
    @Builder.Default
    private Set<County> counties = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "vendor_event_type_coverage",
            joinColumns = @JoinColumn(name = "vendor_id"),
            inverseJoinColumns = @JoinColumn(name = "event_type_id")
    )
    @Builder.Default
    private Set<EventType> eventTypes = new HashSet<>();

    @OneToMany(mappedBy = "vendor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VendorPhoto> photos = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}