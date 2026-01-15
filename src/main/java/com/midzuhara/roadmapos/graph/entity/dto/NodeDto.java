package com.midzuhara.roadmapos.graph.entity.dto;

public record NodeDto(
        Long id,
        String title,
        Double positionX,
        Double positionY,
        String status
) {
}
