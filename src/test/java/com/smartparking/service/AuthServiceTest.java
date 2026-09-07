package com.smartparking.service;

import com.smartparking.dao.UserDao;
import com.smartparking.model.User;
import com.smartparking.model.enums.Role;
import com.smartparking.util.AppSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying AuthService business logic, input validation, BCrypt password hashing,
 * AppSession integration, and error handling using an in-memory FakeUserDao.
 */
public class AuthServiceTest {

    private FakeUserDao fakeUserDao;
    private AuthService authService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        AppSession.getInstance().logout();
        fakeUserDao = new FakeUserDao();
        authService = new AuthService(fakeUserDao);
        userService = new UserService(fakeUserDao);
    }

    // =========================================================================
    // REGISTRATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Successful registration saves user, hashes password, and normalizes fields")
    void testSuccessfulRegistration() {
        User registered = authService.register(
                "  Arun Kumar  ",
                "  Arun.Kumar@Campus.Edu  ",
                "  +919876543210  ",
                "SecureP@ss123",
                "  TN 09 BX 4512  "
        );

        assertNotNull(registered);
        assertNotNull(registered.getUserId());
        assertEquals(1, registered.getUserId());
        assertEquals("Arun Kumar", registered.getFullName());
        assertEquals("arun.kumar@campus.edu", registered.getEmail());
        assertEquals("+919876543210", registered.getPhoneNumber());
        assertEquals("TN 09 BX 4512", registered.getVehicleNumber());
        assertEquals(Role.CUSTOMER, registered.getRole());

        // Verify password is hashed with BCrypt and raw password is NOT stored
        assertNotEquals("SecureP@ss123", registered.getPasswordHash());
        assertTrue(BCrypt.checkpw("SecureP@ss123", registered.getPasswordHash()));
        assertTrue(registered.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    @DisplayName("Registration with blank or null vehicle number stores null")
    void testRegistrationWithNullOrBlankVehicleNumber() {
        User u1 = authService.register("Jane Doe", "jane@example.com", "9876543210", "Password123", null);
        assertNull(u1.getVehicleNumber());

        User u2 = authService.register("John Doe", "john@example.com", "9876543211", "Password123", "   ");
        assertNull(u2.getVehicleNumber());
    }

    @Test
    @DisplayName("Registration rejects duplicate email addresses case-insensitively")
    void testDuplicateEmailRejection() {
        authService.register("First User", "student@college.edu", "9876543210", "Password123", null);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("Second User", "STUDENT@COLLEGE.EDU", "9876543211", "Password456", null)
        );
        assertEquals("Email is already registered.", ex.getMessage());
    }

    @Test
    @DisplayName("Registration validates full name constraints")
    void testFullNameValidation() {
        // Null or blank
        assertThrows(ValidationException.class, () ->
                authService.register(null, "test@test.com", "9876543210", "Password123", null));
        assertThrows(ValidationException.class, () ->
                authService.register("   ", "test@test.com", "9876543210", "Password123", null));

        // Too short (< 2)
        assertThrows(ValidationException.class, () ->
                authService.register("A", "test@test.com", "9876543210", "Password123", null));

        // Too long (> 100)
        String longName = "A".repeat(101);
        assertThrows(ValidationException.class, () ->
                authService.register(longName, "test@test.com", "9876543210", "Password123", null));

        // Invalid characters (e.g. numbers or symbols like <>)
        assertThrows(ValidationException.class, () ->
                authService.register("Arun123", "test@test.com", "9876543210", "Password123", null));
        assertThrows(ValidationException.class, () ->
                authService.register("Arun <script>", "test@test.com", "9876543210", "Password123", null));

        // Valid names with hyphen, period, apostrophe, and Unicode
        assertDoesNotThrow(() ->
                authService.register("Dr. Jean-Luc O'Connor", "jean@test.com", "9876543210", "Password123", null));
    }

    @Test
    @DisplayName("Registration validates email format and length")
    void testEmailValidation() {
        // Null or blank
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", null, "9876543210", "Password123", null));
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "   ", "9876543210", "Password123", null));

        // Too long (> 120 chars)
        String longEmail = "a".repeat(115) + "@test.com";
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", longEmail, "9876543210", "Password123", null));

        // Invalid formats
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "student", "9876543210", "Password123", null));
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "student@", "9876543210", "Password123", null));
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "student@college", "9876543210", "Password123", null));
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "student @gmail.com", "9876543210", "Password123", null));
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "student@@gmail.com", "9876543210", "Password123", null));
    }

    @Test
    @DisplayName("Registration validates phone number rules")
    void testPhoneNumberValidation() {
        // Null or blank
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", null, "Password123", null));
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "   ", "Password123", null));

        // Less than 10 digits
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "98765", "Password123", null));

        // More than 15 digits
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "1234567890123456", "Password123", null));

        // Alphabetic characters
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "+91 ABC1234567", "Password123", null));

        // Valid phone numbers (10 digits, + with 10-15 digits)
        assertDoesNotThrow(() ->
                authService.register("Valid One", "a1@b.com", "9876543210", "Password123", null));
        assertDoesNotThrow(() ->
                authService.register("Valid Two", "a2@b.com", "+919876543210", "Password123", null));
        assertDoesNotThrow(() ->
                authService.register("Valid Three", "a3@b.com", "+14155552671", "Password123", null));
    }

    @Test
    @DisplayName("Registration validates password requirements without trimming spaces")
    void testPasswordValidation() {
        // Null or empty
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "9876543210", null, null));
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "9876543210", "", null));

        // Under 8 characters
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "9876543210", "1234567", null));

        // Over 72 characters
        String longPass = "P".repeat(73);
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "9876543210", longPass, null));

        // Valid passwords with exactly 8 and 72 characters
        assertDoesNotThrow(() ->
                authService.register("Valid Name 8", "pass8@b.com", "9876543210", "12345678", null));
        assertDoesNotThrow(() ->
                authService.register("Valid Name 72", "pass72@b.com", "9876543210", "P".repeat(72), null));
    }

    @Test
    @DisplayName("Registration validates vehicle number constraints")
    void testVehicleNumberValidation() {
        // Exceeds 30 characters
        String longVehicle = "V".repeat(31);
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "9876543210", "Password123", longVehicle));

        // Contains invalid symbols
        assertThrows(ValidationException.class, () ->
                authService.register("Valid Name", "a@b.com", "9876543210", "Password123", "TN#09$1234"));

        // Valid vehicle numbers
        assertDoesNotThrow(() ->
                authService.register("Valid V1", "v1@b.com", "9876543210", "Password123", "TN 09 BX 4512"));
        assertDoesNotThrow(() ->
                authService.register("Valid V2", "v2@b.com", "9876543210", "Password123", "MH-12-DE-1433"));
    }

    // =========================================================================
    // LOGIN & AUTHENTICATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Login with valid credentials logs into AppSession and returns User")
    void testSuccessfulLogin() {
        authService.register("Arun Kumar", "arun@test.com", "9876543210", "MySecretPass!99", "TN 01 AB 1234");

        assertFalse(authService.isLoggedIn());

        // Login with mixed case email and verify normalization
        User loggedIn = authService.login("  ARUN@TEST.COM  ", "MySecretPass!99");

        assertNotNull(loggedIn);
        assertEquals("arun@test.com", loggedIn.getEmail());
        assertTrue(authService.isLoggedIn());
        assertTrue(authService.isCustomer());
        assertFalse(authService.isAdmin());
        assertEquals("arun@test.com", authService.getCurrentUser().getEmail());
    }

    @Test
    @DisplayName("Login with invalid password throws generic AuthenticationException")
    void testLoginWithInvalidPassword() {
        authService.register("Arun Kumar", "arun@test.com", "9876543210", "MySecretPass!99", null);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                authService.login("arun@test.com", "WrongPassword")
        );
        assertEquals("Invalid email or password.", ex.getMessage());
        assertFalse(authService.isLoggedIn());
    }

    @Test
    @DisplayName("Login with non-existent email throws generic AuthenticationException")
    void testLoginWithNonExistentEmail() {
        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                authService.login("unknown@test.com", "AnyPassword123")
        );
        assertEquals("Invalid email or password.", ex.getMessage());
        assertFalse(authService.isLoggedIn());
    }

    @Test
    @DisplayName("Login rejects empty or null email or password")
    void testLoginEmptyFields() {
        assertThrows(ValidationException.class, () -> authService.login("", "Password123"));
        assertThrows(ValidationException.class, () -> authService.login(null, "Password123"));
        assertThrows(ValidationException.class, () -> authService.login("test@test.com", ""));
        assertThrows(ValidationException.class, () -> authService.login("test@test.com", null));
    }

    @Test
    @DisplayName("Logout clears user session")
    void testLogout() {
        authService.register("Arun Kumar", "arun@test.com", "9876543210", "Password123", null);
        authService.login("arun@test.com", "Password123");
        assertTrue(authService.isLoggedIn());

        authService.logout();
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());
    }

    @Test
    @DisplayName("Admin user session check correctly detects admin role")
    void testAdminSessionCheck() {
        // Create an admin directly in the DAO
        User admin = new User(null, "Administrator", "admin@campus.edu",
                BCrypt.hashpw("AdminPass123", BCrypt.gensalt()),
                "+919876543219", Role.ADMIN, null, LocalDateTime.now());
        fakeUserDao.save(admin);

        authService.login("admin@campus.edu", "AdminPass123");
        assertTrue(authService.isLoggedIn());
        assertTrue(authService.isAdmin());
        assertFalse(authService.isCustomer());
    }

    // =========================================================================
    // USER SERVICE TESTS
    // =========================================================================

    @Test
    @DisplayName("UserService updates customer profile and syncs active session")
    void testUpdateProfile() {
        User reg = authService.register("Original Name", "user@test.com", "9876543210", "Password123", "TN 01 A 1111");
        authService.login("user@test.com", "Password123");

        User updated = userService.updateProfile(reg.getUserId(), "New Name", "9876543299", "TN 01 B 2222");
        assertEquals("New Name", updated.getFullName());
        assertEquals("9876543299", updated.getPhoneNumber());
        assertEquals("TN 01 B 2222", updated.getVehicleNumber());

        // Verify session was synchronized
        assertEquals("New Name", authService.getCurrentUser().getFullName());
        assertEquals("9876543299", authService.getCurrentUser().getPhoneNumber());
        assertEquals("TN 01 B 2222", authService.getCurrentUser().getVehicleNumber());
    }

    // =========================================================================
    // IN-MEMORY FAKE DAO FOR ISOLATED TESTING
    // =========================================================================

    private static class FakeUserDao implements UserDao {
        private final Map<Integer, User> store = new HashMap<>();
        private final AtomicInteger idSequence = new AtomicInteger(1);

        @Override
        public Optional<User> findById(int userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            if (email == null) return Optional.empty();
            String lower = email.toLowerCase();
            return store.values().stream()
                    .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(lower))
                    .findFirst();
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<User> findByRole(Role role) {
            return store.values().stream()
                    .filter(u -> u.getRole() == role)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean existsByEmail(String email) {
            if (email == null) return false;
            String lower = email.toLowerCase();
            return store.values().stream()
                    .anyMatch(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(lower));
        }

        @Override
        public int save(User user) {
            int id = idSequence.getAndIncrement();
            user.setUserId(id);
            // Store a copy or reference
            store.put(id, user);
            return id;
        }

        @Override
        public boolean update(User user) {
            if (user == null || user.getUserId() == null || !store.containsKey(user.getUserId())) {
                return false;
            }
            store.put(user.getUserId(), user);
            return true;
        }

        @Override
        public boolean deleteById(int userId) {
            return store.remove(userId) != null;
        }
    }
}
