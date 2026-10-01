package fi.haagahelia.tablereservation.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import fi.haagahelia.tablereservation.domain.AppUser;
import fi.haagahelia.tablereservation.domain.Reservation;
import fi.haagahelia.tablereservation.domain.ReservationStatus;
import fi.haagahelia.tablereservation.domain.RestaurantTable;
import fi.haagahelia.tablereservation.domain.Role;

/**
 * Tests the availability query against a real (in-memory H2) database.
 * @Transactional makes every test roll back its changes when it finishes.
 */
@SpringBootTest
@Transactional
class RestaurantTableRepositoryTest {

    @Autowired
    private RestaurantTableRepository tableRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private AppUserRepository userRepository;

    private final LocalDate date = LocalDate.now().plusDays(1);
    private final LocalTime slot = LocalTime.of(18, 0);

    private AppUser guest;
    private RestaurantTable smallTable;   // 2 seats
    private RestaurantTable bigTable;     // 6 seats

    @BeforeEach
    void setUp() {
        // The app fills the database with sample data at startup,
        // so we clear it to know exactly what each test starts with.
        reservationRepository.deleteAll();
        tableRepository.deleteAll();
        userRepository.deleteAll();

        guest = userRepository.save(new AppUser("Test Guest", "guest@test.com", "hash", Role.USER));
        smallTable = tableRepository.save(new RestaurantTable(1, 2, true));
        bigTable = tableRepository.save(new RestaurantTable(2, 6, true));
    }

    @Test
    void allTablesAreFreeWhenNothingIsBooked() {
        List<RestaurantTable> result =
                tableRepository.findAvailable(date, slot, 2, ReservationStatus.CONFIRMED);

        assertEquals(2, result.size());
    }

    @Test
    void bookedTableIsNotAvailable() {
        reservationRepository.save(new Reservation(date, slot, 2, guest, smallTable));

        List<RestaurantTable> result =
                tableRepository.findAvailable(date, slot, 2, ReservationStatus.CONFIRMED);

        assertEquals(1, result.size());
        assertEquals(bigTable.getId(), result.get(0).getId());
    }

    @Test
    void bookedTableIsStillFreeInAnotherTimeSlot() {
        reservationRepository.save(new Reservation(date, slot, 2, guest, smallTable));

        List<RestaurantTable> result =
                tableRepository.findAvailable(date, LocalTime.of(20, 0), 2, ReservationStatus.CONFIRMED);

        assertEquals(2, result.size());
    }

    @Test
    void cancelledReservationFreesTheTable() {
        Reservation reservation = reservationRepository.save(new Reservation(date, slot, 2, guest, smallTable));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        List<RestaurantTable> result =
                tableRepository.findAvailable(date, slot, 2, ReservationStatus.CONFIRMED);

        assertEquals(2, result.size());
    }

    @Test
    void tablesWithTooFewSeatsAreNotReturned() {
        List<RestaurantTable> result =
                tableRepository.findAvailable(date, slot, 4, ReservationStatus.CONFIRMED);

        assertEquals(1, result.size());
        assertEquals(bigTable.getId(), result.get(0).getId());
    }

    @Test
    void disabledTableIsNotReturned() {
        smallTable.setActive(false);
        tableRepository.save(smallTable);

        List<RestaurantTable> result =
                tableRepository.findAvailable(date, slot, 2, ReservationStatus.CONFIRMED);

        assertEquals(1, result.size());
        assertEquals(bigTable.getId(), result.get(0).getId());
    }
}