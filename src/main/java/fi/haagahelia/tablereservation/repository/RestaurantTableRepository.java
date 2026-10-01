package fi.haagahelia.tablereservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import fi.haagahelia.tablereservation.domain.ReservationStatus;
import fi.haagahelia.tablereservation.domain.RestaurantTable;

public interface RestaurantTableRepository extends CrudRepository<RestaurantTable, Long> {

    // Used to stop staff from adding two tables with the same number
    boolean existsByTableNumber(int tableNumber);

    /**
     * Finds active tables with enough seats that have no confirmed
     * reservation on the given date and time slot.
     */
    @Query("""
           SELECT t FROM RestaurantTable t
           WHERE t.active = true
             AND t.seats >= :guests
             AND t.id NOT IN (
                 SELECT r.table.id FROM Reservation r
                 WHERE r.date = :date
                   AND r.startTime = :startTime
                   AND r.status = :status)
           ORDER BY t.seats
           """)
    List<RestaurantTable> findAvailable(@Param("date") LocalDate date,
                                        @Param("startTime") LocalTime startTime,
                                        @Param("guests") int guests,
                                        @Param("status") ReservationStatus status);
}