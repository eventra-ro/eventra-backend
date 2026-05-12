package com.eventra.repository;

import com.eventra.entity.VendorPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VendorPhotoRepository extends JpaRepository<VendorPhoto, UUID> {

    List<VendorPhoto> findByVendorIdOrderBySortOrderAsc(UUID vendorId);

    int countByVendorId(UUID vendorId);

    void deleteByVendorIdAndId(UUID vendorId, UUID photoId);
}