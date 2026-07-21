package io.github.aquerr.rentacar.domain.reservation.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Idempotency ledger row for a processed payment webhook. The UNIQUE constraint on
 * {@code payment_reference} (see V14 migration) is what makes webhook processing exactly-once.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_event")
public class PaymentEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "payment_reference", nullable = false, unique = true)
    private String paymentReference;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "outcome", nullable = false)
    private String outcome;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
