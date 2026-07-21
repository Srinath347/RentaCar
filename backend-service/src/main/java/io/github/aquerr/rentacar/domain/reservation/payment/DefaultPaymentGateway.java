package io.github.aquerr.rentacar.domain.reservation.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Production no-op gateway so the application context wires. Tests replace this bean with an
 * in-memory double that records capture/refund invocation counts.
 */
@Component
public class DefaultPaymentGateway implements PaymentGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPaymentGateway.class);

    @Override
    public void capture(String paymentReference) {
        LOGGER.info("capture payment {}", paymentReference);
    }

    @Override
    public void refund(String paymentReference) {
        LOGGER.info("refund payment {}", paymentReference);
    }
}
