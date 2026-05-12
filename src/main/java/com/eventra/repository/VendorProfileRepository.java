package com.eventra.repository;

import com.eventra.entity.VendorProfile;
import com.eventra.entity.enums.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, UUID> {

    Optional<VendorProfile> findByUserId(UUID userId);

    Optional<VendorProfile> findBySlug(String slug);

    boolean existsByUserId(UUID userId);

    @Query("""
        SELECT v FROM VendorProfile v
        LEFT JOIN v.categories c
        LEFT JOIN v.counties co
        WHERE v.status = :status
        AND (:categoryCode IS NULL OR c.code = :categoryCode)
        AND (:countyCode IS NULL OR co.code = :countyCode)
        """)
    Page<VendorProfile> search(
            VendorStatus status,
            String categoryCode,
            String countyCode,
            Pageable pageable);
}