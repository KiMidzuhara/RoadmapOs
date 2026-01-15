package com.midzuhara.roadmapos.graph.dto;

import java.util.List;

public record BatchUpdatePositionRequest(List<SingleUpdate> updates) {
    public record SingleUpdate(Long nodeId, Double x, Double y) {}
}
