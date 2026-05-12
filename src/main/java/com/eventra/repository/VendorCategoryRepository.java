package com.eventra.repository;

import com.eventra.entity.VendorCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorCategoryRepository
        extends JpaRepository<VendorCategory, UUID> {

    List<VendorCategory> findAllByIsActiveTrueOrderBySortOrderAsc();

    Optional<VendorCategory> findByCode(String code);
}