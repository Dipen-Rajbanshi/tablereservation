package fi.haagahelia.tablereservation.repository;

import fi.haagahelia.tablereservation.domain.AppUser;
import fi.haagahelia.tablereservation.domain.Reservation;
import org.springframework.data.repository.CrudRepository;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {

    // "My reservations" page for a guest
    List<Reservation> findByUser(AppUser user);

    // Staff view: all reservations of one day, sorted by time
    List<Reservation> findByDateOrderByStartTime(LocalDate date);
}
