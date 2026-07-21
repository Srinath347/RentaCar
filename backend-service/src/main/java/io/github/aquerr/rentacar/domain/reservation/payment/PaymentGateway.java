package io.github.aquerr.rentacar.domain.reservation.payment;

/**
 * External payment provider (mockable). Its methods are money-moving SIDE EFFECTS and MUST each be
 * invoked at most once per payment reference, regardless of duplicate/late webhooks.
 */
public interface PaymentGateway {
    /** Capture (settle) the authorized payment for a successfully claimed hold. */
    void capture(String paymentReference);

    /** Refund a payment that arrived for an expired / terminal hold that cannot be honoured. */
    void refund(String paymentReference);
}
