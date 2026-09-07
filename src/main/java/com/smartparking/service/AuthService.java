package com.smartparking.service;

import com.smartparking.dao.DaoException;
import com.smartparking.dao.UserDao;
import com.smartparking.dao.jdbc.JdbcUserDao;
import com.smartparking.model.User;
import com.smartparking.model.enums.Role;
import com.smartparking.util.AppSession;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Authentication and User Security Service.
 * <p>
 * Handles customer registration with strict validation, BCrypt password hashing,
 * credential verification, session management via {@link AppSession}, and role enforcement.
 * Completely independent of JavaFX UI components.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\s.'\\-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");
    private static final Pattern VEHICLE_PATTERN = Pattern.compile("^[A-Za-z0-9\\s-]+$");

    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 100;
    private static final int EMAIL_MAX_LENGTH = 120;
    private static final int PHONE_MAX_LENGTH = 20;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 72;
    private static final int VEHICLE_MAX_LENGTH = 30;

    private final UserDao userDao;

    /**
     * Default constructor for production use; instantiates standard {@link JdbcUserDao}.
     */
    public AuthService() {
        this(new JdbcUserDao());
    }

    /**
     * Constructor dependency injection for unit testing and modular configuration.
     *
     * @param userDao persistence DAO for User entities
     */
    public AuthService(UserDao userDao) {
        if (userDao == null) {
            throw new IllegalArgumentException("UserDao must not be null.");
        }
        this.userDao = userDao;
    }

    /**
     * Registers a new customer account in the system with strict field validation,
     * duplicate email prevention, BCrypt hashing, and enforced {@link Role#CUSTOMER}.
     *
     * @param fullName      user's full name (2-100 characters)
     * @param email         user's email address (max 120 characters)
     * @param phoneNumber   user's phone number (10-15 digits, optional leading +)
     * @param password      user's raw plaintext password (8-72 characters)
     * @param vehicleNumber optional vehicle registration number (max 30 characters)
     * @return the newly registered and persisted {@link User} entity
     * @throws ValidationException if any validation check or duplicate email check fails
     * @throws ServiceException    if persistence fails
     */
    public User register(String fullName, String email, String phoneNumber, 
                         String password, String vehicleNumber) {
        String normalizedName = validateAndNormalizeFullName(fullName);
        String normalizedEmail = validateAndNormalizeEmail(email);
        String normalizedPhone = validateAndNormalizePhoneNumber(phoneNumber);
        validatePassword(password);
        String normalizedVehicle = validateAndNormalizeVehicleNumber(vehicleNumber);

        // Duplicate email prevention
        try {
            if (this.userDao.existsByEmail(normalizedEmail)) {
                logger.warn("Registration rejected: email '{}' is already registered.", normalizedEmail);
                throw new ValidationException("Email is already registered.");
            }
        } catch (DaoException e) {
            logger.error("Database error while checking email existence", e);
            throw new ServiceException("Failed to verify email availability. Please try again.", e);
        }

        // Secure password hashing with BCrypt
        String passwordHash = hashPassword(password);

        // Enforce Role.CUSTOMER (public registration can never choose ADMIN)
        User user = new User();
        user.setFullName(normalizedName);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHash);
        user.setPhoneNumber(normalizedPhone);
        user.setRole(Role.CUSTOMER);
        user.setVehicleNumber(normalizedVehicle);
        user.setCreatedAt(LocalDateTime.now());

        try {
            int generatedId = this.userDao.save(user);
            user.setUserId(generatedId);
            logger.info("Successfully registered new customer with ID: {}", generatedId);
            return user;
        } catch (DaoException e) {
            logger.error("Failed to save registered user into database", e);
            throw new ServiceException("Registration could not be completed. Please try again later.", e);
        }
    }

    /**
     * Authenticates a user by email and password.
     * On success, registers the user into {@link AppSession} and returns the authenticated User entity.
     * On failure, throws a generic {@link AuthenticationException} without disclosing whether the email exists.
     *
     * @param email    user's email
     * @param password raw password
     * @return the authenticated {@link User}
     * @throws ValidationException     if credentials are empty or null
     * @throws AuthenticationException if authentication fails (invalid credentials)
     * @throws ServiceException        if a database error occurs
     */
    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required.");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password is required.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        Optional<User> userOptional;
        try {
            userOptional = this.userDao.findByEmail(normalizedEmail);
        } catch (DaoException e) {
            logger.error("Database error while authenticating user", e);
            throw new ServiceException("Authentication service is temporarily unavailable. Please try again later.", e);
        }

        if (userOptional.isEmpty()) {
            logger.warn("Authentication failed: user not found for normalized email");
            throw new AuthenticationException("Invalid email or password.");
        }

        User user = userOptional.get();

        if (!verifyPassword(password, user.getPasswordHash())) {
            logger.warn("Authentication failed: password mismatch for user ID {}", user.getUserId());
            throw new AuthenticationException("Invalid email or password.");
        }

        // Populate global session state
        AppSession.getInstance().login(user);
        logger.info("User ID {} ({}) logged in successfully with role {}", 
                user.getUserId(), user.getEmail(), user.getRole());

        return user;
    }

    /**
     * Clears the current user from {@link AppSession}.
     */
    public void logout() {
        User user = AppSession.getInstance().getCurrentUser();
        if (user != null) {
            logger.info("User ID {} ({}) logged out.", user.getUserId(), user.getEmail());
        }
        AppSession.getInstance().logout();
    }

    /**
     * Returns whether a user is currently authenticated in the global session.
     */
    public boolean isLoggedIn() {
        return AppSession.getInstance().isLoggedIn();
    }

    /**
     * Returns the currently authenticated user from {@link AppSession}.
     */
    public User getCurrentUser() {
        return AppSession.getInstance().getCurrentUser();
    }

    /**
     * Checks if the active logged-in user possesses the {@link Role#ADMIN} role.
     */
    public boolean isAdmin() {
        return AppSession.getInstance().isAdmin();
    }

    /**
     * Checks if the active logged-in user possesses the {@link Role#CUSTOMER} role.
     */
    public boolean isCustomer() {
        return AppSession.getInstance().isCustomer();
    }

    /**
     * Hashes a raw plaintext password using jBCrypt with a generated salt.
     *
     * @param rawPassword the plaintext password
     * @return the BCrypt hash string
     */
    public String hashPassword(String rawPassword) {
        validatePassword(rawPassword);
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * Verifies a raw plaintext password against a stored BCrypt hash.
     *
     * @param rawPassword the plaintext password to verify
     * @param storedHash  the stored BCrypt password hash
     * @return true if password matches hash, false otherwise
     */
    public boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, storedHash);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid BCrypt hash format encountered during password verification");
            return false;
        }
    }

    // =========================================================================
    // FIELD VALIDATION & NORMALIZATION
    // =========================================================================

    /**
     * Validates and trims full name.
     * Must be 2-100 characters and contain valid name characters.
     */
    public String validateAndNormalizeFullName(String fullName) {
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

    /**
     * Validates and normalizes email (trimmed and converted to lowercase).
     * Must be max 120 characters and conform to practical email format.
     */
    public String validateAndNormalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required.");
        }
        String trimmed = email.trim();
        if (trimmed.length() > EMAIL_MAX_LENGTH) {
            throw new ValidationException("Email must not exceed 120 characters.");
        }
        if (trimmed.contains(" ") || !EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
        return trimmed.toLowerCase();
    }

    /**
     * Validates and normalizes phone number (trimmed).
     * Must be 10-15 digits with an optional leading '+'.
     */
    public String validateAndNormalizePhoneNumber(String phoneNumber) {
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

    /**
     * Validates raw password without trimming or converting case.
     * Length must be between 8 and 72 characters inclusive.
     */
    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password is required.");
        }
        if (password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            throw new ValidationException("Password must be between 8 and 72 characters.");
        }
    }

    /**
     * Validates and normalizes vehicle number.
     * Optional; null or blank returns null.
     * If present, maximum 30 characters and alphanumeric with spaces and hyphens.
     */
    public String validateAndNormalizeVehicleNumber(String vehicleNumber) {
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
