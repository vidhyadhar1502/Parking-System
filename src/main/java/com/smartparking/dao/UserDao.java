package com.smartparking.dao;

import com.smartparking.model.User;
import com.smartparking.model.enums.Role;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for {@link User} entity persistence operations.
 */
public interface UserDao {

    /**
     * Finds a user by primary key ID.
     *
     * @param userId unique user identifier
     * @return an Optional containing the User if found, empty otherwise
     */
    Optional<User> findById(int userId);

    /**
     * Finds a user by unique email address.
     *
     * @param email user email
     * @return an Optional containing the User if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves all registered users in descending registration order.
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Retrieves users filtered by system Role (CUSTOMER or ADMIN).
     *
     * @param role user role enum
     * @return list of matching users
     */
    List<User> findByRole(Role role);

    /**
     * Checks if a user already exists with the specified email address.
     *
     * @param email user email address to test
     * @return true if email is registered, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Inserts a new user record into the database.
     * Generates and assigns the auto-increment user_id on the passed User entity.
     *
     * @param user the User entity to persist
     * @return the generated primary key user_id
     */
    int save(User user);

    /**
     * Updates an existing user's profile details (full_name, phone_number, role, vehicle_number).
     *
     * @param user the User entity with updated fields
     * @return true if the row was updated, false if no record matched
     */
    boolean update(User user);

    /**
     * Deletes a user record by primary key ID.
     *
     * @param userId unique user identifier
     * @return true if deleted, false if no record was found
     */
    boolean deleteById(int userId);
}
