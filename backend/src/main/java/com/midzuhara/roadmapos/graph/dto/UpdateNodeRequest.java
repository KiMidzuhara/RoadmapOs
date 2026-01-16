package com.midzuhara.roadmapos.graph.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNodeRequest(
        @Size(max = 255, message = "Title must be less than 255 characters")
        String title,
        String status
) {
}
