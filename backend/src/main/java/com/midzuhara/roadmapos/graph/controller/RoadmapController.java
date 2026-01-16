package com.midzuhara.roadmapos.graph.controller;

import com.midzuhara.roadmapos.graph.dto.CreateRoadmapRequest;
import com.midzuhara.roadmapos.graph.dto.RoadmapResponseDto;
import com.midzuhara.roadmapos.graph.dto.UpdateRoadmapRequest;
import com.midzuhara.roadmapos.graph.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roadmaps")
@CrossOrigin(origins = "http://localhost:5173")
public class RoadmapController {

    private final RoadmapService roadmapService;

    @PostMapping
    public ResponseEntity<RoadmapResponseDto> create(@RequestBody @Valid CreateRoadmapRequest request) {
        RoadmapResponseDto created = roadmapService.create(request);
        // Возвращаем 201 Created вместо 200 OK
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoadmapResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(roadmapService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<RoadmapResponseDto>> getAll() {
        return ResponseEntity.ok(roadmapService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoadmapResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRoadmapRequest request) {
        return ResponseEntity.ok(roadmapService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content - тело ответа пустое
    public void delete(@PathVariable Long id) {
        roadmapService.delete(id);
    }
}
