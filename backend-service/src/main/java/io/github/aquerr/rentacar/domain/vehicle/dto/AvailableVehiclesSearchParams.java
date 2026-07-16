package io.github.aquerr.rentacar.domain.vehicle.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value(staticConstructor = "of")
public class AvailableVehiclesSearchParams
{
    LocalDateTime from;
    LocalDateTime to;
    int page;
    int size;
}
