package org.tokenization.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "AJ_PARTY") // Maps this class to your AJ_PARTY table
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    // We will add username, password, role in a later phase

    @Column(name = "KYC_LEVEL")
    private String kycLevel;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_AT", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}