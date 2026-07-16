package io.github.aquerr.rentacar.domain.reservation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ProfileReservation {
    private Long id;
    private String vehicleIconUrl;
    private String vehicleName;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private String status;
}
