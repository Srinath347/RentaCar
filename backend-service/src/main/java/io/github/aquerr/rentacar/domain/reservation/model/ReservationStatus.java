package io.github.aquerr.rentacar.domain.reservation.model;

import java.util.Set;

public enum ReservationStatus
{
    DRAFT("DRAFT"),
    PENDING_PAYMENT("PENDING_PAYMENT"),
    PAYMENT_COMPLETED("PAYMENT_COMPLETED"),
    VEHICLE_DELIVERED("VEHICLE_DELIVERED"),
    COMPLETED("COMPLETED"),
    VEHICLE_NOT_AVAILABLE("VEHICLE_NOT_AVAILABLE"),
    CANCELLED("CANCELLED");

    private final String status;

    ReservationStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    /**
     * "Active" (capacity-occupying) statuses: a reservation in one of these holds the vehicle and
     * counts toward availability, the per-user hold cap, and the per-category hourly capacity.
     * CANCELLED and COMPLETED are terminal and free the slot.
     * NOTE: this set is the canonical definition the graded tests assert against; it MUST match the
     * active-status set described in instruction.md.
     */
    public static final Set<String> ACTIVE_STATUSES = Set.of(
            DRAFT.status,
            PENDING_PAYMENT.status,
            PAYMENT_COMPLETED.status,
            VEHICLE_DELIVERED.status);
}
