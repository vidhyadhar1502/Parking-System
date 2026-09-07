package com.smartparking.dao;

import com.smartparking.model.ParkingSlot;
import com.smartparking.model.enums.SlotStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for individual {@link ParkingSlot} bay entities.
 */
public interface ParkingSlotDao {

    /**
     * Finds a parking slot by its primary key ID.
     *
     * @param slotId unique slot identifier
     * @return an Optional containing the ParkingSlot if found, empty otherwise
     */
    Optional<ParkingSlot> findById(int slotId);

    /**
     * Retrieves all parking slots across all facilities.
     *
     * @return list of all parking slots
     */
    List<ParkingSlot> findAll();

    /**
     * Retrieves all parking slots situated within a specific parking facility.
     *
     * @param locationId facility foreign key ID
     * @return list of parking slots in the facility
     */
    List<ParkingSlot> findByLocationId(int locationId);

    /**
     * Retrieves parking slots for a facility filtered by their real-time operational status.
     *
     * @param locationId facility foreign key ID
     * @param status operational status filter (e.g., AVAILABLE, OCCUPIED)
     * @return list of matching parking slots
     */
    List<ParkingSlot> findByLocationAndStatus(int locationId, SlotStatus status);

    /**
     * Retrieves parking slots situated on a specific floor deck within a facility.
     *
     * @param locationId facility foreign key ID
     * @param floorLevel floor deck number (1, 2, 3...)
     * @return list of matching parking slots
     */
    List<ParkingSlot> findByFloor(int locationId, int floorLevel);

    /**
     * Counts the total number of slots matching a given status within a facility.
     * Useful for high-performance capacity KPI widgets.
     *
     * @param locationId facility foreign key ID
     * @param status operational status to count
     * @return count of matching slots
     */
    long countByLocationAndStatus(int locationId, SlotStatus status);

    /**
     * Updates only the operational status of an individual parking slot.
     *
     * @param slotId slot primary key ID
     * @param status new operational status
     * @return true if updated, false if slot was not found
     */
    boolean updateStatus(int slotId, SlotStatus status);

    /**
     * Inserts a new parking bay record into the database.
     * Generates and assigns the auto-increment slot_id on the passed entity.
     *
     * @param slot the ParkingSlot entity to persist
     * @return the generated primary key slot_id
     */
    int save(ParkingSlot slot);

    /**
     * Updates an existing parking bay's attributes (slot number, floor level, type, status).
     *
     * @param slot the ParkingSlot entity with updated fields
     * @return true if updated, false if no record matched
     */
    boolean update(ParkingSlot slot);

    /**
     * Deletes a parking bay by primary key ID.
     *
     * @param slotId unique slot identifier
     * @return true if deleted, false if no record was found
     */
    boolean deleteById(int slotId);

    /**
     * Finds and locks a parking slot row using MySQL {@code SELECT ... FOR UPDATE}
     * within the context of an existing external database transaction.
     * <p>
     * <b>Note:</b> Does NOT open or close the Connection; the caller owns the transaction boundary.
     *
     * @param connection the active transactional SQL connection
     * @param slotId unique slot identifier to lock
     * @return an Optional containing the locked ParkingSlot, or empty if not found
     */
    Optional<ParkingSlot> findByIdForUpdate(Connection connection, int slotId);
}
