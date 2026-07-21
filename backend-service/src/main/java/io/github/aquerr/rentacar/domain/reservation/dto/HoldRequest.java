package io.github.aquerr.rentacar.domain.reservation.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A payment-hold request: a user reserves one or more CATEGORY/hour slots at once (a specific vehicle
 * is assigned later, at claim/delivery). The whole batch is expected to be applied all-or-nothing.
 */
@Data
public class HoldRequest {
    private Long userId;
    private List<Item> items;

    @Data
    public static class Item {
        private String category;
        private LocalDateTime dateFrom;
        private LocalDateTime dateTo;
    }
}
