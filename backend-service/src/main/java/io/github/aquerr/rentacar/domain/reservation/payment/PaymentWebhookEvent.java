package io.github.aquerr.rentacar.domain.reservation.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound async payment confirmation. {@code paymentReference} is the provider-side idempotency key:
 * the same confirmation may be delivered multiple times (duplicate webhooks) and may arrive after the
 * hold has expired or been cancelled.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookEvent {
    private String paymentReference;
    private Long reservationId;
}
