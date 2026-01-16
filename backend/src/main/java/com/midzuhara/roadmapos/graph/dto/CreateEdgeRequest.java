package com.midzuhara.roadmapos.graph.dto;

import jakarta.validation.constraints.NotNull;

public record CreateEdgeRequest(
        @NotNull(message = "Source node ID is required")
        Long sourceNodeId,
        @NotNull(message = "Target node ID is required")
        Long targetNodeId
) {
}
