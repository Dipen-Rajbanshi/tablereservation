package fi.haagahelia.tablereservation.domain;

/** Status of a reservation. Cancelled ones stay in the database but free the table. */
public enum ReservationStatus {
    CONFIRMED, CANCELLED
}
