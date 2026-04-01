package com.dts.restro.support.service;

import com.dts.restro.audit.service.AuditService;
import com.dts.restro.auth.entity.User;
import com.dts.restro.auth.repository.UserRepository;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.restaurant.entity.Restaurant;
import com.dts.restro.support.dto.CreateTicketRequest;
import com.dts.restro.support.dto.TicketCommentRequest;
import com.dts.restro.support.dto.UpdateTicketRequest;
import com.dts.restro.support.entity.SupportTicket;
import com.dts.restro.support.entity.SupportTicketComment;
import com.dts.restro.support.enums.TicketStatus;
import com.dts.restro.support.repository.SupportTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class SupportTicketService {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketService.class);

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public SupportTicketService(SupportTicketRepository ticketRepository,
                               UserRepository userRepository,
                               AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    /**
     * Create a new support ticket
     */
    public SupportTicket createTicket(CreateTicketRequest request) {
        User currentUser = getCurrentUser();
        Restaurant restaurant = currentUser.getRestaurant();

        if (restaurant == null) {
            throw new IllegalStateException("User must belong to a restaurant to create tickets");
        }

        String ticketNumber = generateTicketNumber();

        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(ticketNumber)
                .restaurant(restaurant)
                .createdBy(currentUser)
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority())
                .category(request.getCategory())
                .status(TicketStatus.OPEN)
                .build();

        SupportTicket saved = ticketRepository.save(ticket);
        
        auditService.logAsync("CREATE_TICKET", "SUPPORT_TICKET", saved.getId(), null, saved);
        log.info("Support ticket created: {} by user: {}", ticketNumber, currentUser.getUsername());

        return saved;
    }

    /**
     * Get ticket by ID
     */
    @Transactional(readOnly = true)
    public SupportTicket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    /**
     * Get ticket by ticket number
     */
    @Transactional(readOnly = true)
    public SupportTicket getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketNumber));
    }

    /**
     * Get all tickets for a restaurant
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getRestaurantTickets(Long restaurantId, Pageable pageable) {
        return ticketRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable);
    }

    /**
     * Get all tickets (Super Admin only)
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    /**
     * Get tickets by status
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    /**
     * Get open tickets (for Super Admin dashboard)
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getOpenTickets(Pageable pageable) {
        List<TicketStatus> openStatuses = List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.REOPENED);
        return ticketRepository.findByStatusInOrderByPriorityDescCreatedAtAsc(openStatuses, pageable);
    }

    /**
     * Get tickets assigned to a user
     */
    @Transactional(readOnly = true)
    public Page<SupportTicket> getAssignedTickets(Long userId, Pageable pageable) {
        return ticketRepository.findByAssignedToIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Update ticket
     */
    public SupportTicket updateTicket(Long id, UpdateTicketRequest request) {
        SupportTicket ticket = getTicketById(id);
        SupportTicket oldTicket = cloneTicket(ticket);

        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
            if (request.getStatus() == TicketStatus.RESOLVED || request.getStatus() == TicketStatus.CLOSED) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
        }

        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        if (request.getAssignedToUserId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            ticket.setAssignedTo(assignedTo);
        }

        SupportTicket updated = ticketRepository.save(ticket);
        auditService.logAsync("UPDATE_TICKET", "SUPPORT_TICKET", updated.getId(), oldTicket, updated);

        return updated;
    }

    /**
     * Add comment to ticket
     */
    public SupportTicketComment addComment(Long ticketId, TicketCommentRequest request) {
        SupportTicket ticket = getTicketById(ticketId);
        User currentUser = getCurrentUser();

        SupportTicketComment comment = SupportTicketComment.builder()
                .ticket(ticket)
                .user(currentUser)
                .comment(request.getComment())
                .isInternal(request.getIsInternal() != null ? request.getIsInternal() : false)
                .build();

        ticket.addComment(comment);
        ticketRepository.save(ticket);

        auditService.logAsync("ADD_TICKET_COMMENT", "SUPPORT_TICKET", ticketId, null, comment);

        return comment;
    }

    /**
     * Assign ticket to user
     */
    public SupportTicket assignTicket(Long ticketId, Long userId) {
        SupportTicket ticket = getTicketById(ticketId);
        User assignedTo = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User previousAssignee = ticket.getAssignedTo();
        ticket.setAssignedTo(assignedTo);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        SupportTicket updated = ticketRepository.save(ticket);
        
        auditService.logAsync("ASSIGN_TICKET", "SUPPORT_TICKET", ticketId, 
                previousAssignee != null ? previousAssignee.getId() : null, 
                assignedTo.getId());

        return updated;
    }

    /**
     * Close ticket
     */
    public SupportTicket closeTicket(Long ticketId) {
        SupportTicket ticket = getTicketById(ticketId);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setResolvedAt(LocalDateTime.now());

        SupportTicket updated = ticketRepository.save(ticket);
        auditService.logAsync("CLOSE_TICKET", "SUPPORT_TICKET", ticketId, null, updated);

        return updated;
    }

    /**
     * Reopen ticket
     */
    public SupportTicket reopenTicket(Long ticketId) {
        SupportTicket ticket = getTicketById(ticketId);
        ticket.setStatus(TicketStatus.REOPENED);
        ticket.setResolvedAt(null);

        SupportTicket updated = ticketRepository.save(ticket);
        auditService.logAsync("REOPEN_TICKET", "SUPPORT_TICKET", ticketId, null, updated);

        return updated;
    }

    /**
     * Get ticket statistics
     */
    @Transactional(readOnly = true)
    public TicketStatistics getStatistics() {
        long totalOpen = ticketRepository.countByStatus(TicketStatus.OPEN);
        long totalInProgress = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long totalResolved = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long totalClosed = ticketRepository.countByStatus(TicketStatus.CLOSED);

        return new TicketStatistics(totalOpen, totalInProgress, totalResolved, totalClosed);
    }

    // ── Helper Methods ──────────────────────────────────────────────────────

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private String generateTicketNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "TKT-" + timestamp + "-" + random;
    }

    private SupportTicket cloneTicket(SupportTicket ticket) {
        return SupportTicket.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .assignedTo(ticket.getAssignedTo())
                .build();
    }

    // Inner class for statistics
    public static class TicketStatistics {
        public final long open;
        public final long inProgress;
        public final long resolved;
        public final long closed;

        public TicketStatistics(long open, long inProgress, long resolved, long closed) {
            this.open = open;
            this.inProgress = inProgress;
            this.resolved = resolved;
            this.closed = closed;
        }
    }
}

// Made with Bob
