package com.dts.restro.superadmin.service;

import com.dts.restro.audit.service.AuditService;
import com.dts.restro.auth.entity.User;
import com.dts.restro.auth.repository.UserRepository;
import com.dts.restro.billing.repository.BillRepository;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.menu.repository.MenuItemRepository;
import com.dts.restro.order.repository.KOTRepository;
import com.dts.restro.order.repository.RestaurantTableRepository;
import com.dts.restro.restaurant.entity.Restaurant;
import com.dts.restro.restaurant.repository.RestaurantRepository;
import com.dts.restro.superadmin.dto.RestaurantStatsDTO;
import com.dts.restro.superadmin.dto.SystemStatsDTO;
import com.dts.restro.support.enums.TicketStatus;
import com.dts.restro.support.repository.SupportTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class SuperAdminService {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminService.class);

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final SupportTicketRepository ticketRepository;
    private final BillRepository billRepository;
    private final KOTRepository kotRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final AuditService auditService;

    public SuperAdminService(RestaurantRepository restaurantRepository,
                            UserRepository userRepository,
                            SupportTicketRepository ticketRepository,
                            BillRepository billRepository,
                            KOTRepository kotRepository,
                            MenuItemRepository menuItemRepository,
                            RestaurantTableRepository tableRepository,
                            AuditService auditService) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.billRepository = billRepository;
        this.kotRepository = kotRepository;
        this.menuItemRepository = menuItemRepository;
        this.tableRepository = tableRepository;
        this.auditService = auditService;
    }

    // ── Restaurant Management ────────────────────────────────────────────────

    /**
     * Get all restaurants with pagination
     */
    @Transactional(readOnly = true)
    public Page<Restaurant> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable);
    }

    /**
     * Get system-wide statistics
     */
    @Transactional(readOnly = true)
    public SystemStatsDTO getSystemStatistics() {
        long totalRestaurants = restaurantRepository.count();
        long activeRestaurants = restaurantRepository.countByActiveTrue();
        long inactiveRestaurants = totalRestaurants - activeRestaurants;
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        
        long openTickets = ticketRepository.countByStatus(TicketStatus.OPEN);
        long inProgressTickets = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long resolvedTickets = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        
        // Calculate total revenue across all restaurants
        Double totalRevenue = billRepository.findAll().stream()
                .mapToDouble(bill -> bill.getTotal() != null ? bill.getTotal() : 0.0)
                .sum();
        
        // Today's statistics
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        long todayOrders = kotRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        
        return SystemStatsDTO.builder()
                .totalRestaurants(totalRestaurants)
                .activeRestaurants(activeRestaurants)
                .inactiveRestaurants(inactiveRestaurants)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .openTickets(openTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .totalRevenue(totalRevenue)
                .todayOrders(todayOrders)
                .build();
    }

    /**
     * Get statistics for a specific restaurant
     */
    @Transactional(readOnly = true)
    public RestaurantStatsDTO getRestaurantStatistics(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        long totalUsers = userRepository.countByRestaurantId(restaurantId);
        long activeUsers = userRepository.countByRestaurantIdAndActiveTrue(restaurantId);
        long totalTables = tableRepository.countByRestaurantId(restaurantId);
        long totalMenuItems = menuItemRepository.countByRestaurantId(restaurantId);
        
        // Calculate total orders and revenue
        long totalOrders = kotRepository.countByRestaurantId(restaurantId);
        Double totalRevenue = billRepository.findByRestaurantId(restaurantId).stream()
                .mapToDouble(bill -> bill.getTotal() != null ? bill.getTotal() : 0.0)
                .sum();
        
        // Today's statistics
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        long todayOrders = kotRepository.countByRestaurantIdAndCreatedAtBetween(
                restaurantId, startOfDay, endOfDay);
        
        long openTickets = ticketRepository.countByRestaurantAndStatus(
                restaurantId, TicketStatus.OPEN);
        
        // Get last activity from audit logs
        LocalDateTime lastActivity = auditService.getRestaurantActivityCount(
                restaurantId, LocalDateTime.now().minusDays(1)) > 0 
                ? LocalDateTime.now() : null;

        return RestaurantStatsDTO.builder()
                .restaurantId(restaurantId)
                .restaurantName(restaurant.getName())
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalTables(totalTables)
                .totalMenuItems(totalMenuItems)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .todayOrders(todayOrders)
                .openTickets(openTickets)
                .lastActivity(lastActivity)
                .isActive(restaurant.getActive())
                .build();
    }

    /**
     * Activate a restaurant
     */
    public Restaurant activateRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setActive(true);
        Restaurant updated = restaurantRepository.save(restaurant);

        auditService.logAsync("ACTIVATE_RESTAURANT", "RESTAURANT", restaurantId, false, true);
        log.info("Restaurant activated: id={} name={}", restaurantId, restaurant.getName());

        return updated;
    }

    /**
     * Deactivate a restaurant
     */
    public Restaurant deactivateRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setActive(false);
        Restaurant updated = restaurantRepository.save(restaurant);

        auditService.logAsync("DEACTIVATE_RESTAURANT", "RESTAURANT", restaurantId, true, false);
        log.info("Restaurant deactivated: id={} name={}", restaurantId, restaurant.getName());

        return updated;
    }

    // ── User Management ──────────────────────────────────────────────────────

    /**
     * Get all users across all restaurants
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Get users for a specific restaurant
     */
    @Transactional(readOnly = true)
    public List<User> getRestaurantUsers(Long restaurantId) {
        return userRepository.findByRestaurantId(restaurantId);
    }

    /**
     * Activate a user
     */
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(true);
        User updated = userRepository.save(user);

        auditService.logAsync("ACTIVATE_USER", "USER", userId, false, true);
        log.info("User activated: id={} username={}", userId, user.getUsername());

        return updated;
    }

    /**
     * Deactivate a user
     */
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(false);
        User updated = userRepository.save(user);

        auditService.logAsync("DEACTIVATE_USER", "USER", userId, true, false);
        log.info("User deactivated: id={} username={}", userId, user.getUsername());

        return updated;
    }
}

// Made with Bob
