package com.midzuhara.roadmapos.graph.entity.dto;

public record EdgeDto(
        Long id,
        String source,
        String target
) {
}
