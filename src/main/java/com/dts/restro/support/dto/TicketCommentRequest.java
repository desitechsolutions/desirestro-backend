package com.dts.restro.support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketCommentRequest {
    @NotBlank(message = "Comment is required")
    private String comment;

    private Boolean isInternal;
}

// Made with Bob
