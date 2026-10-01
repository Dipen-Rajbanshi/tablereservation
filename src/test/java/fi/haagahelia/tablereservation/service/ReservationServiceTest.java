package fi.haagahelia.tablereservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fi.haagahelia.tablereservation.domain.AppUser;
import fi.haagahelia.tablereservation.domain.Reservation;
import fi.haagahelia.tablereservation.domain.ReservationStatus;
import fi.haagahelia.tablereservation.domain.RestaurantTable;
import fi.haagahelia.tablereservation.domain.Role;
import fi.haagahelia.tablereservation.repository.AppUserRepository;
import fi.haagahelia.tablereservation.repository.ReservationRepository;
import fi.haagahelia.tablereservation.repository.RestaurantTableRepository;

/**
 * Tests the booking and cancelling rules. The repositories are mocks (fake objects),
 * so no database is needed and the tests run in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private RestaurantTableRepository tableRepository;
    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private ReservationService service;

    private final LocalDate tomorrow = LocalDate.now().plusDays(1);
    private final LocalTime slot = LocalTime.of(18, 0);

    private AppUser user() {
        return new AppUser("Test Guest", "guest@test.com", "hash", Role.USER);
    }

    private RestaurantTable table() {
        RestaurantTable table = new RestaurantTable(1, 2, true);
        table.setId(1L);
        return table;
    }

    @Test
    void bookingADateInThePastIsRejected() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThrows(IllegalStateException.class,
                () -> service.book("guest@test.com", 1L, yesterday, slot, 2));
    }

    @Test
    void bookingATableThatIsNoLongerFreeIsRejected() {
        when(userRepository.findByEmail("guest@test.com")).thenReturn(Optional.of(user()));
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table()));
        // The availability query returns nothing: the table has been taken
        when(tableRepository.findAvailable(any(), any(), anyInt(), any())).thenReturn(List.of());

        assertThrows(IllegalStateException.class,
                () -> service.book("guest@test.com", 1L, tomorrow, slot, 2));

        // Nothing must be saved when the booking fails
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void successfulBookingIsConfirmedAndBelongsToTheUser() {
        AppUser user = user();
        RestaurantTable table = table();
        when(userRepository.findByEmail("guest@test.com")).thenReturn(Optional.of(user));
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(tableRepository.findAvailable(any(), any(), anyInt(), any())).thenReturn(List.of(table));
        // Make the fake save() return whatever it was given
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(call -> call.getArgument(0));

        Reservation result = service.book("guest@test.com", 1L, tomorrow, slot, 2);

        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
        assertEquals(user, result.getUser());
        assertEquals(table, result.getTable());
    }

    @Test
    void userCannotCancelSomeoneElsesReservation() {
        Reservation reservation = new Reservation(tomorrow, slot, 2, user(), table());
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> service.cancel("someone.else@test.com", 5L));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void ownerCanCancelTheirReservation() {
        Reservation reservation = new Reservation(tomorrow, slot, 2, user(), table());
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        service.cancel("guest@test.com", 5L);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }
}