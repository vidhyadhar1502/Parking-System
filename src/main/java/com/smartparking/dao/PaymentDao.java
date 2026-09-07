package com.smartparking.dao;

import com.smartparking.model.Payment;
import com.smartparking.model.enums.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for {@link Payment} transaction and billing records.
 */
public interface PaymentDao {

    /**
     * Finds a payment record by its auto-generated primary key ID.
     *
     * @param paymentId unique payment identifier
     * @return an Optional containing the Payment if found, empty otherwise
     */
    Optional<Payment> findById(int paymentId);

    /**
     * Finds a payment record associated with a specific reservation.
     *
     * @param reservationId unique reservation foreign key ID
     * @return an Optional containing the Payment if found, empty otherwise
     */
    Optional<Payment> findByReservationId(int reservationId);

    /**
     * Retrieves all payment records in reverse chronological order.
     *
     * @return list of all payment records
     */
    List<Payment> findAll();

    /**
     * Retrieves payment records filtered by their financial settlement status (PAID, PENDING, WAIVED).
     *
     * @param status payment settlement status
     * @return list of matching payment records
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Inserts a new payment billing record into the database.
     * Generates and assigns the auto-increment payment_id on the passed entity.
     *
     * @param payment the Payment entity to persist
     * @return the generated primary key payment_id
     */
    int save(Payment payment);

    /**
     * Updates an existing payment record (duration, amount, status, method, paid_at).
     *
     * @param payment the Payment entity with updated fields
     * @return true if updated, false if no record matched
     */
    boolean update(Payment payment);

    /**
     * Updates only the settlement status of an individual payment.
     *
     * @param paymentId unique payment identifier
     * @param status new settlement status
     * @return true if updated, false if payment was not found
     */
    boolean updateStatus(int paymentId, PaymentStatus status);

    /**
     * Deletes a payment record by primary key ID.
     *
     * @param paymentId unique payment identifier
     * @return true if deleted, false if no record was found
     */
    boolean deleteById(int paymentId);
}
