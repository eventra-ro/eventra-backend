package com.eventra.repository;

import com.eventra.entity.County;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CountyRepository extends JpaRepository<County, UUID> {

    List<County> findAllByOrderByNameAsc();

    Optional<County> findByCode(String code);
}