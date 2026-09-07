package com.smartparking.service;

import com.smartparking.dao.DaoException;
import com.smartparking.dao.UserDao;
import com.smartparking.dao.jdbc.JdbcUserDao;
import com.smartparking.model.User;
import com.smartparking.model.enums.Role;
import com.smartparking.util.AppSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service managing user profile data, administrative user management, and vehicle details.
 * Focuses on non-authentication user operations.
 */
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\s.'\\-]+$");
    private static final Pattern VEHICLE_PATTERN = Pattern.compile("^[A-Za-z0-9\\s-]+$");

    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int PHONE_MAX_LENGTH = 20;
    private static final int VEHICLE_MAX_LENGTH = 30;

    private final UserDao userDao;

    /**
     * Default constructor instantiating standard {@link JdbcUserDao}.
     */
    public UserService() {
        this(new JdbcUserDao());
    }

    /**
     * Constructor dependency injection.
     *
     * @param userDao persistence DAO for User entities
     */
    public UserService(UserDao userDao) {
        if (userDao == null) {
            throw new IllegalArgumentException("UserDao must not be null.");
        }
        this.userDao = userDao;
    }

    /**
     * Retrieves user profile by unique ID.
     *
     * @param userId primary key ID
     * @return User if found
     * @throws ValidationException if user does not exist
     * @throws ServiceException    if persistence query fails
     */
    public User getUserById(int userId) {
        try {
            return this.userDao.findById(userId)
                    .orElseThrow(() -> new ValidationException("User not found with ID: " + userId));
        } catch (DaoException e) {
            logger.error("Failed to query user by ID {}", userId, e);
            throw new ServiceException("Unable to retrieve user profile.", e);
        }
    }

    /**
     * Retrieves user by email address.
     *
     * @param email user email
     * @return Optional containing User if found
     */
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return this.userDao.findByEmail(email.trim().toLowerCase());
        } catch (DaoException e) {
            logger.error("Failed to query user by email {}", email, e);
            throw new ServiceException("Unable to retrieve user by email.", e);
        }
    }

    /**
     * Updates an existing user's profile details.
     * Synchronizes active session if the updated user is currently logged in.
     *
     * @param userId        ID of the user to update
     * @param fullName      new full name
     * @param phoneNumber   new phone number
     * @param vehicleNumber optional new vehicle number
     * @return updated User entity
     */
    public User updateProfile(int userId, String fullName, String phoneNumber, String vehicleNumber) {
        User user = getUserById(userId);

        String normalizedName = validateAndNormalizeFullName(fullName);
        String normalizedPhone = validateAndNormalizePhoneNumber(phoneNumber);
        String normalizedVehicle = validateAndNormalizeVehicleNumber(vehicleNumber);

        user.setFullName(normalizedName);
        user.setPhoneNumber(normalizedPhone);
        user.setVehicleNumber(normalizedVehicle);

        try {
            boolean updated = this.userDao.update(user);
            if (!updated) {
                throw new ServiceException("Failed to update user profile in database.");
            }
            logger.info("Updated profile for user ID: {}", userId);

            // Synchronize active AppSession if modifying current logged-in user
            User currentUser = AppSession.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getUserId() != null && currentUser.getUserId() == userId) {
                currentUser.setFullName(normalizedName);
                currentUser.setPhoneNumber(normalizedPhone);
                currentUser.setVehicleNumber(normalizedVehicle);
            }

            return user;
        } catch (DaoException e) {
            logger.error("Database update error for user ID {}", userId, e);
            throw new ServiceException("Could not update profile. Please try again.", e);
        }
    }

    /**
     * Updates only the vehicle registration number for a customer.
     *
     * @param userId        user ID
     * @param vehicleNumber new vehicle number
     * @return updated User entity
     */
    public User updateVehicleNumber(int userId, String vehicleNumber) {
        User user = getUserById(userId);
        String normalizedVehicle = validateAndNormalizeVehicleNumber(vehicleNumber);
        user.setVehicleNumber(normalizedVehicle);

        try {
            this.userDao.update(user);
            logger.info("Updated vehicle number for user ID: {}", userId);

            User currentUser = AppSession.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getUserId() != null && currentUser.getUserId() == userId) {
                currentUser.setVehicleNumber(normalizedVehicle);
            }

            return user;
        } catch (DaoException e) {
            logger.error("Database update error for vehicle number of user ID {}", userId, e);
            throw new ServiceException("Could not update vehicle number. Please try again.", e);
        }
    }

    /**
     * Retrieves all users in the system (for admin dashboard).
     *
     * @return list of all users
     */
    public List<User> getAllUsers() {
        try {
            return this.userDao.findAll();
        } catch (DaoException e) {
            logger.error("Failed to retrieve all users", e);
            throw new ServiceException("Unable to fetch user directory.", e);
        }
    }

    /**
     * Retrieves users filtered by system role.
     *
     * @param role user role
     * @return list of matching users
     */
    public List<User> getUsersByRole(Role role) {
        if (role == null) {
            return getAllUsers();
        }
        try {
            return this.userDao.findByRole(role);
        } catch (DaoException e) {
            logger.error("Failed to retrieve users by role {}", role, e);
            throw new ServiceException("Unable to fetch users by role.", e);
        }
    }

    private String validateAndNormalizeFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name is required.");
        }
        String trimmed = fullName.trim();
        if (trimmed.length() < NAME_MIN_LENGTH || trimmed.length() > NAME_MAX_LENGTH) {
            throw new ValidationException("Full name must be between 2 and 100 characters.");
        }
        if (!NAME_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Full name contains invalid characters.");
        }
        return trimmed;
    }

    private String validateAndNormalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new ValidationException("Phone number is required.");
        }
        String trimmed = phoneNumber.trim();
        if (trimmed.length() > PHONE_MAX_LENGTH) {
            throw new ValidationException("Phone number must contain 10 to 15 digits.");
        }

        String digitsOnly = trimmed.startsWith("+") ? trimmed.substring(1) : trimmed;
        if (!digitsOnly.matches("^\\d{10,15}$")) {
            throw new ValidationException("Phone number must contain 10 to 15 digits.");
        }
        return trimmed;
    }

    private String validateAndNormalizeVehicleNumber(String vehicleNumber) {
        if (vehicleNumber == null || vehicleNumber.trim().isEmpty()) {
            return null;
        }
        String trimmed = vehicleNumber.trim();
        if (trimmed.length() > VEHICLE_MAX_LENGTH) {
            throw new ValidationException("Vehicle number must not exceed 30 characters.");
        }
        if (!VEHICLE_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Invalid vehicle number format.");
        }
        return trimmed;
    }
}
