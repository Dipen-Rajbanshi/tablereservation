package fi.haagahelia.tablereservation.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/** One booking: a guest reserves a table for a fixed 2-hour slot on a date. */
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private LocalTime startTime;    // slot start, e.g. 18:00 (the slot lasts 2 hours)
    private int guests;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @ManyToOne                      // many reservations belong to one user
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne                      // many reservations belong to one table
    @JoinColumn(name = "table_id")
    private RestaurantTable table;

    public Reservation() {
    }

    public Reservation(LocalDate date, LocalTime startTime, int guests, AppUser user, RestaurantTable table) {
        this.date = date;
        this.startTime = startTime;
        this.guests = guests;
        this.user = user;
        this.table = table;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public RestaurantTable getTable() {
        return table;
    }

    public void setTable(RestaurantTable table) {
        this.table = table;
    }
}