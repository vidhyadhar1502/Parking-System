package com.smartparking.dao;

import com.smartparking.model.Reservation;
import com.smartparking.model.enums.ReservationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for {@link Reservation} session and booking records.
 */
public interface ReservationDao {

    /**
     * Finds a reservation by its auto-generated primary key ID.
     *
     * @param reservationId unique reservation identifier
     * @return an Optional containing the Reservation if found, empty otherwise
     */
    Optional<Reservation> findById(int reservationId);

    /**
     * Finds a reservation by its unique public reservation code / UUID token.
     *
     * @param reservationCode unique alphanumeric booking code
     * @return an Optional containing the Reservation if found, empty otherwise
     */
    Optional<Reservation> findByCode(String reservationCode);

    /**
     * Retrieves all reservations in reverse chronological order.
     *
     * @return list of all reservations
     */
    List<Reservation> findAll();

    /**
     * Retrieves all reservations created by a specific user.
     *
     * @param userId user primary key ID
     * @return list of reservations for the user
     */
    List<Reservation> findByUserId(int userId);

    /**
     * Retrieves all reservations historical and present associated with a specific slot.
     *
     * @param slotId slot primary key ID
     * @return list of reservations for the slot
     */
    List<Reservation> findBySlotId(int slotId);

    /**
     * Retrieves reservations filtered by their lifecycle status.
     *
     * @param status reservation status (e.g., CONFIRMED, CHECKED_IN)
     * @return list of matching reservations
     */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Retrieves currently active reservations for a user (status IN ('CONFIRMED', 'CHECKED_IN')).
     *
     * @param userId user primary key ID
     * @return list of active reservations for the user
     */
    List<Reservation> findActiveByUserId(int userId);

    /**
     * Inserts a new reservation record into the database.
     * Generates and assigns the auto-increment reservation_id on the passed entity.
     *
     * @param reservation the Reservation entity to persist
     * @return the generated primary key reservation_id
     */
    int save(Reservation reservation);

    /**
     * Updates an existing reservation record (timestamps, status, qr code path).
     *
     * @param reservation the Reservation entity with updated attributes
     * @return true if updated, false if no record matched
     */
    boolean update(Reservation reservation);

    /**
     * Updates only the lifecycle status of a reservation.
     *
     * @param reservationId unique reservation identifier
     * @param status new lifecycle status
     * @return true if updated, false if reservation was not found
     */
    boolean updateStatus(int reservationId, ReservationStatus status);

    /**
     * Deletes a reservation record by primary key ID.
     *
     * @param reservationId unique reservation identifier
     * @return true if deleted, false if no record was found
     */
    boolean deleteById(int reservationId);
}
