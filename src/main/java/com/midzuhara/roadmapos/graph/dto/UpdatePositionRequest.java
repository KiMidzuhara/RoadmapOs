package com.midzuhara.roadmapos.graph.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePositionRequest(
        @NotNull(message = "X coordinate is required")
        Double x,
        @NotNull(message = "Y coordinate is required")
        Double y
) {
}
