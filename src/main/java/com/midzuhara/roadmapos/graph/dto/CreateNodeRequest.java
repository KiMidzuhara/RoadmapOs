package com.midzuhara.roadmapos.graph.dto;

import jakarta.validation.constraints.NotNull;

public record CreateNodeRequest(
        @NotNull(message = "Title is required")
        String title,
        @NotNull(message = "X coordinate is required")
        Double x,
        @NotNull(message = "Y coordinate is required")
        Double y
) {
}
