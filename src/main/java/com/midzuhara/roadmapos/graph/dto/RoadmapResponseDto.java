package com.midzuhara.roadmapos.graph.dto;

import java.util.List;

public record RoadmapResponseDto(
        Long id,
        String title,
        String description,
        List<NodeDto> nodes,
        List<EdgeDto> edges
) {
}
