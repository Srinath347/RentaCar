package io.github.aquerr.rentacar.domain.reservation.repository;

import io.github.aquerr.rentacar.domain.reservation.model.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    // Vehicle ids with an ACTIVE reservation overlapping the half-open interval [dateFrom, dateTo).
    // Overlap := existing.dateFrom < :dateTo AND :dateFrom < existing.dateTo (adjacent hours do NOT conflict).
    @Query("SELECT r.vehicleId FROM ReservationEntity r " +
            "WHERE r.status IN :activeStatuses " +
            "AND r.dateFrom < :dateTo AND :dateFrom < r.dateTo")
    List<Integer> findAllNotAvailableVehiclesBetweenDates(@Param("dateFrom") LocalDateTime dateFrom,
                                                          @Param("dateTo") LocalDateTime dateTo,
                                                          @Param("activeStatuses") Collection<String> activeStatuses);

    // Active reservations for a single vehicle overlapping [dateFrom, dateTo).
    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.vehicleId = :vehicleId " +
            "AND r.status IN :activeStatuses " +
            "AND r.dateFrom < :dateTo AND :dateFrom < r.dateTo")
    List<ReservationEntity> findOverlappingForVehicle(@Param("vehicleId") int vehicleId,
                                                      @Param("dateFrom") LocalDateTime dateFrom,
                                                      @Param("dateTo") LocalDateTime dateTo,
                                                      @Param("activeStatuses") Collection<String> activeStatuses);

    @Query("FROM ReservationEntity reservation WHERE reservation.userId = :userId")
    List<ReservationEntity> findAllByUserId(@Param("userId") Long userId);

    // Count reservations occupying capacity in a category over the window [windowStart, windowEnd):
    // confirmed rows (PAYMENT_COMPLETED / VEHICLE_DELIVERED) plus LIVE holds (PENDING_PAYMENT whose
    // expiresAt is still in the future). Expired holds and terminal rows are excluded and free the slot.
    @Query("SELECT COUNT(r) FROM ReservationEntity r " +
            "WHERE r.category = :category " +
            "AND r.dateFrom < :windowEnd AND :windowStart < r.dateTo " +
            "AND (r.status IN :confirmedStatuses " +
            "     OR (r.status = :pendingStatus AND r.expiresAt > :now))")
    long countOccupiedInCategoryWindow(@Param("category") String category,
                                       @Param("windowStart") LocalDateTime windowStart,
                                       @Param("windowEnd") LocalDateTime windowEnd,
                                       @Param("confirmedStatuses") Collection<String> confirmedStatuses,
                                       @Param("pendingStatus") String pendingStatus,
                                       @Param("now") LocalDateTime now);
}
