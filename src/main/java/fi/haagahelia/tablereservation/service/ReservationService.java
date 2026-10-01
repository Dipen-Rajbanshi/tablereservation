package fi.haagahelia.tablereservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.haagahelia.tablereservation.domain.AppUser;
import fi.haagahelia.tablereservation.domain.Reservation;
import fi.haagahelia.tablereservation.domain.ReservationStatus;
import fi.haagahelia.tablereservation.domain.RestaurantTable;
import fi.haagahelia.tablereservation.repository.AppUserRepository;
import fi.haagahelia.tablereservation.repository.ReservationRepository;
import fi.haagahelia.tablereservation.repository.RestaurantTableRepository;

/** Business logic for reservations: booking a table and listing a guest's bookings. */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final AppUserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              RestaurantTableRepository tableRepository,
                              AppUserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
        this.userRepository = userRepository;
    }

    /**
     * Books a table for the logged-in user. The availability is checked again
     * here, because someone else may have booked the table after the search.
     */
    @Transactional
    public Reservation book(String email, Long tableId, LocalDate date, LocalTime time, int guests) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalStateException("You cannot book a date in the past.");
        }

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found."));
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new IllegalStateException("Table not found."));

        boolean stillFree = tableRepository
                .findAvailable(date, time, guests, ReservationStatus.CONFIRMED)
                .stream()
                .anyMatch(t -> t.getId().equals(tableId));

        if (!stillFree) {
            throw new IllegalStateException("Sorry, that table is no longer available.");
        }

        return reservationRepository.save(new Reservation(date, time, guests, user, table));
    }

    /** All reservations of one user, found by the email they logged in with. */
    public List<Reservation> findByUser(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found."));
        return reservationRepository.findByUser(user);
    }
        /**
     * Cancels a reservation. Only the user who made it may cancel it.
     * The row stays in the database with status CANCELLED, which frees the table.
     */
    @Transactional
    public void cancel(String email, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Reservation not found."));

        // Safety check: the owner is compared with the logged-in user
        if (!reservation.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("You can only cancel your own reservations.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

        /** All reservations of one day, sorted by time. Used by the staff view. */
    public List<Reservation> findByDate(LocalDate date) {
        return reservationRepository.findByDateOrderByStartTime(date);
    }

    /** Cancels any reservation. Staff only: the controller is protected by the ADMIN role. */
    @Transactional
    public void cancelAsStaff(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalStateException("Reservation not found."));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }
}
