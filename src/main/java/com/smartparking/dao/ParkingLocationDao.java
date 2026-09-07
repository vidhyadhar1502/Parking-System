package com.smartparking.dao;

import com.smartparking.model.ParkingLocation;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for {@link ParkingLocation} facility entities.
 */
public interface ParkingLocationDao {

    /**
     * Finds a parking facility by primary key ID.
     *
     * @param locationId unique location identifier
     * @return an Optional containing the ParkingLocation if found, empty otherwise
     */
    Optional<ParkingLocation> findById(int locationId);

    /**
     * Retrieves all parking facilities regardless of operational status.
     *
     * @return list of all parking locations
     */
    List<ParkingLocation> findAll();

    /**
     * Retrieves only currently active parking facilities.
     *
     * @return list of active parking locations
     */
    List<ParkingLocation> findActive();

    /**
     * Inserts a new parking facility record into the database.
     * Generates and assigns the auto-increment location_id on the passed entity.
     *
     * @param location the ParkingLocation entity to persist
     * @return the generated primary key location_id
     */
    int save(ParkingLocation location);

    /**
     * Updates an existing parking facility's attributes (name, address, coordinates, capacity, rate, status).
     *
     * @param location the ParkingLocation entity with updated fields
     * @return true if updated, false if no record matched
     */
    boolean update(ParkingLocation location);

    /**
     * Deletes a parking facility record by primary key ID.
     *
     * @param locationId unique location identifier
     * @return true if deleted, false if no record was found
     */
    boolean deleteById(int locationId);
}
