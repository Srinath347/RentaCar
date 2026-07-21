package io.github.aquerr.rentacar.domain.reservation.model;

import io.github.aquerr.rentacar.domain.user.model.UserEntity;
import io.github.aquerr.rentacar.domain.vehicle.VehicleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
    @Table(name = "reservation")
public class ReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", insertable = false, updatable = false)
    private VehicleEntity vehicle;

    // Nullable: a category-level hold has no concrete vehicle until it is claimed/delivered.
    @Column(name = "vehicle_id")
    private Integer vehicleId;

    // Vehicle category this reservation occupies (set for holds; the pool cap is per category/hour).
    @Column(name = "category")
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "date_from", nullable = false)
    private LocalDateTime dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDateTime dateTo;

    @Column(name = "status", nullable = false)
    private String status;

    // Set to (now + hold TTL) when a PENDING_PAYMENT hold is placed; NULL for non-hold rows.
    // A hold occupies category/hour capacity only while expiresAt is in the future.
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}