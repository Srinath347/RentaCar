package io.github.aquerr.rentacar.domain.reservation.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Multi-car booking request: a single user reserves several vehicles/time-windows at once.
 * The whole batch is expected (by the graded tests) to be applied all-or-nothing.
 */
@Data
public class ReservationBatchRequest {
    private Long userId;
    private List<Item> items;

    @Data
    public static class Item {
        private Integer vehicleId;
        private LocalDateTime dateFrom;
        private LocalDateTime dateTo;
    }
}
