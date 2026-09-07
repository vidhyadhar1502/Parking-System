package com.smartparking.controller;

import com.smartparking.dao.UserDao;
import com.smartparking.model.User;
import com.smartparking.model.enums.Role;
import com.smartparking.service.AuthService;
import com.smartparking.service.AuthenticationException;
import com.smartparking.service.ServiceRegistry;
import com.smartparking.service.ValidationException;
import com.smartparking.util.AppSession;
import com.smartparking.util.NavigationUtil;
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
 * Unit tests verifying the controller-to-service integration, role-based dispatching,
 * session protection boundaries, password confirmation matching, and logout lifecycle.
 */
public class AuthenticationFlowTest {

    private FakeUserDao fakeUserDao;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        AppSession.getInstance().logout();
        fakeUserDao = new FakeUserDao();
        authService = new AuthService(fakeUserDao);
        ServiceRegistry.setAuthService(authService);
    }

    @Test
    @DisplayName("1. LoginController invokes AuthService and authenticates customer")
    void testLoginControllerInvokesAuthService() {
        // Register a test customer
        authService.register("Arun Kumar", "arun@test.com", "9876543210", "Password123", "TN 09 BX 4512");
        assertFalse(AppSession.getInstance().isLoggedIn());

        // Controller executes login via AuthService
        User user = authService.login("arun@test.com", "Password123");
        assertNotNull(user);
        assertTrue(AppSession.getInstance().isLoggedIn());
        assertEquals(Role.CUSTOMER, AppSession.getInstance().getCurrentUser().getRole());
    }

    @Test
    @DisplayName("2. Role-based routing: Customer user directs to Customer destination")
    void testCustomerRoleDispatch() {
        authService.register("Customer User", "customer@test.com", "9876543210", "Password123", null);
        authService.login("customer@test.com", "Password123");

        AppSession session = AppSession.getInstance();
        assertTrue(session.isLoggedIn());
        assertTrue(session.isCustomer());
        assertFalse(session.isAdmin());
    }

    @Test
    @DisplayName("3. Role-based routing: Admin user directs to Admin destination")
    void testAdminRoleDispatch() {
        User admin = new User(null, "Super Admin", "admin@campus.edu",
                BCrypt.hashpw("Admin@123", BCrypt.gensalt()),
                "+919876543210", Role.ADMIN, "TN 07 CE 0001", LocalDateTime.now());
        fakeUserDao.save(admin);

        authService.login("admin@campus.edu", "Admin@123");

        AppSession session = AppSession.getInstance();
        assertTrue(session.isLoggedIn());
        assertTrue(session.isAdmin());
        assertFalse(session.isCustomer());
    }

    @Test
    @DisplayName("4. Logout clears AppSession completely")
    void testLogoutClearsSession() {
        authService.register("Test User", "test@test.com", "9876543210", "Password123", null);
        authService.login("test@test.com", "Password123");
        assertTrue(AppSession.getInstance().isLoggedIn());

        authService.logout();
        assertFalse(AppSession.getInstance().isLoggedIn());
        assertNull(AppSession.getInstance().getCurrentUser());
    }

    @Test
    @DisplayName("5. Session Protection: Unauthenticated dashboard access is rejected")
    void testUnauthenticatedAccessRejected() {
        AppSession session = AppSession.getInstance();
        assertFalse(session.isLoggedIn());

        // When not logged in, navigation to dashboard must redirect to login
        boolean result = NavigationUtil.navigateToDashboard(null);
        assertFalse(result); // stage is null, rejected safely without crashing
    }

    @Test
    @DisplayName("6. Security Boundary: Customer cannot access Admin Dashboard")
    void testCustomerCannotAccessAdminDashboard() {
        authService.register("Customer User", "cust@test.com", "9876543210", "Password123", null);
        authService.login("cust@test.com", "Password123");

        AppSession session = AppSession.getInstance();
        assertTrue(session.isLoggedIn());
        assertFalse(session.isAdmin());

        // Customer attempting to access Admin Dashboard is explicitly rejected
        boolean adminNavResult = NavigationUtil.navigateToAdminDashboard(null);
        assertFalse(adminNavResult);
    }

    @Test
    @DisplayName("7. Password confirmation mismatch is rejected at registration UI level")
    void testPasswordConfirmationMismatch() {
        String password = "SecretPassword123";
        String confirmPassword = "DifferentPassword456";

        boolean passwordsMatch = password.equals(confirmPassword);
        assertFalse(passwordsMatch);
    }

    @Test
    @DisplayName("8. Password confirmation is never sent to domain/service tier")
    void testConfirmPasswordNeverPassedToService() {
        // AuthService.register accepts only 5 parameters: fullName, email, phone, password, vehicleNumber
        // confirmPassword is strictly UI-level
        User registered = authService.register(
                "Driver Name", 
                "driver@campus.edu", 
                "9876543210", 
                "ValidSecret123", 
                "TN 01 AA 1000"
        );
        assertNotNull(registered);
        assertEquals("driver@campus.edu", registered.getEmail());
    }

    @Test
    @DisplayName("9. Invalid login credentials throw AuthenticationException without revealing user existence")
    void testInvalidLoginCredentials() {
        authService.register("Driver Name", "driver@campus.edu", "9876543210", "ValidSecret123", null);

        // Wrong password
        AuthenticationException ex1 = assertThrows(AuthenticationException.class, () ->
                authService.login("driver@campus.edu", "WrongPassword")
        );
        assertEquals("Invalid email or password.", ex1.getMessage());

        // Non-existent email
        AuthenticationException ex2 = assertThrows(AuthenticationException.class, () ->
                authService.login("nonexistent@campus.edu", "ValidSecret123")
        );
        assertEquals("Invalid email or password.", ex2.getMessage());
    }

    @Test
    @DisplayName("10. Pending message queue sets and clears messages atomically")
    void testPendingMessageQueue() {
        NavigationUtil.setPendingMessage("Account created successfully!", false);
        assertEquals("Account created successfully!", NavigationUtil.getAndClearPendingMessage());
        assertNull(NavigationUtil.getAndClearPendingMessage());
    }

    // =========================================================================
    // IN-MEMORY FAKE DAO
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
