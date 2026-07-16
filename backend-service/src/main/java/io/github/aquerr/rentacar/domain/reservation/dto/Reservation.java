package io.github.aquerr.rentacar.domain.reservation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class Reservation {
    private Long id;
    private Integer vehicleId;
    private Long userId;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String status;
}
