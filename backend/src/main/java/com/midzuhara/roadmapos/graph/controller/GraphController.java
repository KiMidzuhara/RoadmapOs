package com.midzuhara.roadmapos.graph.controller;

import com.midzuhara.roadmapos.graph.dto.*;
import com.midzuhara.roadmapos.graph.service.GraphService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1") // Общий префикс
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class GraphController {

    private final GraphService graphService;

    // NODES (УЗЛЫ)
    // Создание узла привязано к Roadmap, поэтому путь вложенный
    @PostMapping("/roadmaps/{roadmapId}/nodes")
    public ResponseEntity<NodeDto> addNode(
            @PathVariable Long roadmapId,
            @RequestBody @Valid CreateNodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(graphService.addNode(roadmapId, request));
    }

    // Обновление контента узла
    @PatchMapping("/nodes/{nodeId}")
    public ResponseEntity<NodeDto> updateNodeDetails(
            @PathVariable Long nodeId,
            @RequestBody @Valid UpdateNodeRequest request) {
        return ResponseEntity.ok(graphService.updateNode(nodeId, request));
    }

    // Обновление позиции (Drag & Drop)
    @PutMapping("/nodes/{nodeId}/position")
    public ResponseEntity<NodeDto> updatePosition(
            @PathVariable Long nodeId,
            @RequestBody @Valid UpdatePositionRequest request) {
        return ResponseEntity.ok(graphService.updateNodePosition(nodeId, request));
    }

    // Массовое обновление позиций
    @PutMapping("/nodes/positions/batch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void batchUpdatePositions(@RequestBody @Valid BatchUpdatePositionRequest request) {
        graphService.batchUpdateNodePositions(request);
    }

    @DeleteMapping("/nodes/{nodeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNode(@PathVariable Long nodeId) {
        graphService.deleteNode(nodeId);
    }


    // EDGES (СВЯЗИ)
    @PostMapping("/roadmaps/{roadmapId}/edges")
    public ResponseEntity<EdgeDto> createEdge(
            @PathVariable Long roadmapId,
            @RequestBody @Valid CreateEdgeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(graphService.createEdge(roadmapId, request));
    }

    @DeleteMapping("/edges/{edgeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEdge(@PathVariable Long edgeId) {
        graphService.deleteEdge(edgeId);
    }
}