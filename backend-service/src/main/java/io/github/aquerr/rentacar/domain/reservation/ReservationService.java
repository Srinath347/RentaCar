package io.github.aquerr.rentacar.domain.reservation;

import io.github.aquerr.rentacar.application.exception.ReservationException;
import io.github.aquerr.rentacar.application.exception.ReservationVehicleNotAvailableException;
import io.github.aquerr.rentacar.application.security.AuthenticatedUser;
import io.github.aquerr.rentacar.application.security.AuthenticationFacade;
import io.github.aquerr.rentacar.domain.reservation.converter.ReservationConverter;
import io.github.aquerr.rentacar.domain.reservation.dto.HoldRequest;
import io.github.aquerr.rentacar.domain.reservation.dto.ProfileReservation;
import io.github.aquerr.rentacar.domain.reservation.dto.Reservation;
import io.github.aquerr.rentacar.domain.reservation.dto.ReservationBatchRequest;
import io.github.aquerr.rentacar.domain.reservation.model.ReservationEntity;
import io.github.aquerr.rentacar.domain.reservation.model.ReservationStatus;
import io.github.aquerr.rentacar.domain.reservation.payment.PaymentEventEntity;
import io.github.aquerr.rentacar.domain.reservation.payment.PaymentEventRepository;
import io.github.aquerr.rentacar.domain.reservation.payment.PaymentGateway;
import io.github.aquerr.rentacar.domain.reservation.payment.PaymentWebhookEvent;
import io.github.aquerr.rentacar.domain.vehicle.VehicleService;
import io.github.aquerr.rentacar.domain.vehicle.repository.VehicleRepository;
import io.github.aquerr.rentacar.domain.reservation.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ReservationService {

    // Hold time-to-live: a placed hold occupies category/hour capacity for this long unless claimed.
    private static final Duration HOLD_TTL = Duration.ofMinutes(30);

    private final ReservationRepository reservationRepository;
    private final ReservationConverter reservationConverter;
    private final AuthenticationFacade authenticationFacade;
    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final PaymentGateway paymentGateway;

    public List<Long> placeHold(HoldRequest request) {
        List<Long> ids = new ArrayList<>();
        for (HoldRequest.Item item : request.getItems()) {
            ids.add(placeSingleHold(request.getUserId(), item));
        }
        return ids;
    }

    @Transactional
    protected Long placeSingleHold(Long userId, HoldRequest.Item item) {
        long pool = vehicleRepository.countByCategory(item.getCategory());
        long occupied = reservationRepository.countOccupiedInCategoryWindow(
                item.getCategory(),
                item.getDateFrom(),
                item.getDateFrom().plusHours(1),
                ReservationStatus.CONFIRMED_STATUSES,
                ReservationStatus.PENDING_PAYMENT.getStatus(),
                LocalDateTime.now());
        if (occupied >= pool) {
            throw new ReservationVehicleNotAvailableException();
        }
        ReservationEntity entity = ReservationEntity.builder()
                .category(item.getCategory())
                .userId(userId)
                .dateFrom(item.getDateFrom())
                .dateTo(item.getDateTo())
                .status(ReservationStatus.PENDING_PAYMENT.getStatus())
                .expiresAt(LocalDateTime.now().plus(HOLD_TTL))
                .build();
        return reservationRepository.save(entity).getId();
    }

    @Transactional
    public void confirmPayment(PaymentWebhookEvent event) {
        if (paymentEventRepository.findByPaymentReference(event.getPaymentReference()).isPresent()) {
            return;
        }
        ReservationEntity reservation = reservationRepository.findById(event.getReservationId())
                .orElseThrow(ReservationException::new);
        if (ReservationStatus.PENDING_PAYMENT.getStatus().equals(reservation.getStatus())) {
            paymentGateway.capture(event.getPaymentReference());
            reservation.setStatus(ReservationStatus.PAYMENT_COMPLETED.getStatus());
            reservation.setExpiresAt(null);
            reservationRepository.save(reservation);
        }
        paymentEventRepository.save(PaymentEventEntity.builder()
                .paymentReference(event.getPaymentReference())
                .reservationId(event.getReservationId())
                .outcome(reservation.getStatus())
                .processedAt(LocalDateTime.now())
                .build());
    }

    // ---------------------------------------------------------------------------------------------
    // Existing single-reservation API (unchanged).
    // ---------------------------------------------------------------------------------------------

    @Transactional
    public Reservation save(Reservation reservation) {
        if (vehicleService.isVehicleAvailable(reservation.getVehicleId(), reservation.getDateFrom(), reservation.getDateTo())) {
            ReservationEntity reservationEntity = reservationConverter.toReservationEntity(reservation);
            return reservationConverter.toReservationDto(this.reservationRepository.save(reservationEntity));
        }
        if (reservation.getId() != null) {
            if (!ReservationStatus.CANCELLED.getStatus().equals(reservation.getStatus())) {
                reservation.setStatus(ReservationStatus.VEHICLE_NOT_AVAILABLE.getStatus());
            }
            ReservationEntity reservationEntity = reservationConverter.toReservationEntity(reservation);
            return reservationConverter.toReservationDto(this.reservationRepository.save(reservationEntity));
        }
        throw new ReservationVehicleNotAvailableException();
    }

    public List<Reservation> saveBatch(ReservationBatchRequest request) {
        List<Reservation> results = new ArrayList<>();
        for (ReservationBatchRequest.Item item : request.getItems()) {
            Reservation reservation = Reservation.builder()
                    .vehicleId(item.getVehicleId())
                    .userId(request.getUserId())
                    .dateFrom(item.getDateFrom())
                    .dateTo(item.getDateTo())
                    .status(ReservationStatus.DRAFT.getStatus())
                    .build();
            results.add(save(reservation));
        }
        return results;
    }

    public Reservation getReservation(Long reservationId) {
        List<ReservationEntity> userReservations = getMyReservations();
        return userReservations.stream()
                .map(reservationConverter::toReservationDto)
                .filter(reservation -> reservationId.equals(reservation.getId()))
                .findFirst()
                .orElseThrow(ReservationException::new);
    }

    public void updateReservationStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = getReservation(reservationId);
        reservation.setStatus(status.getStatus());
        reservationRepository.save(reservationConverter.toReservationEntity(reservation));
    }

    public List<ProfileReservation> getProfileReservations() {
        List<ReservationEntity> myReservations = getMyReservations();
        return myReservations.stream()
                .map(reservationConverter::toProfileReservation)
                .collect(Collectors.toList());
    }

    public List<ProfileReservation> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationConverter::toProfileReservation)
                .collect(Collectors.toList());
    }

    private List<ReservationEntity> getMyReservations() {
        AuthenticatedUser authenticatedUser = authenticationFacade.getCurrentUser();
        return this.reservationRepository.findAllByUserId(authenticatedUser.getId());
    }
}
