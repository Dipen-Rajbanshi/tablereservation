package fi.haagahelia.tablereservation.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

/** A physical table in the restaurant that guests can reserve. */
@Entity
@Table(name = "restaurant_table")   // "table" is a reserved word in SQL, so we rename it
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    private int tableNumber;    // the number shown to guests and staff

    @Min(1)
    private int seats;          // how many guests fit at this table

    private boolean active = true;  // staff can disable a table without deleting it

    // Empty constructor required by JPA
    public RestaurantTable() {
    }

    // Convenience constructor (no id, the database generates it)
    public RestaurantTable(int tableNumber, int seats, boolean active) {
        this.tableNumber = tableNumber;
        this.seats = seats;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}