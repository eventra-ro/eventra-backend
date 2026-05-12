package com.eventra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "event_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}