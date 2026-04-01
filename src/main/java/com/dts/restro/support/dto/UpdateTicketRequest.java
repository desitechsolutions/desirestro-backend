package com.dts.restro.support.dto;

import com.dts.restro.support.enums.TicketPriority;
import com.dts.restro.support.enums.TicketStatus;
import lombok.Data;

@Data
public class UpdateTicketRequest {
    private TicketStatus status;
    private TicketPriority priority;
    private Long assignedToUserId;
}

// Made with Bob
