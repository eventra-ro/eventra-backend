package com.eventra.repository;

import com.eventra.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventTypeRepository extends JpaRepository<EventType, UUID> {

    List<EventType> findAllByIsActiveTrueOrderBySortOrderAsc();

    Optional<EventType> findByCode(String code);
}