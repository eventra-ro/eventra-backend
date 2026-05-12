package com.eventra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "counties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class County {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}