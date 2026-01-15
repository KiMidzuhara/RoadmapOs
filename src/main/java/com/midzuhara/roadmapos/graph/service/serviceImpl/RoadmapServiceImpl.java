package com.midzuhara.roadmapos.graph.service.serviceImpl;

import com.midzuhara.roadmapos.graph.entity.Roadmap;
import com.midzuhara.roadmapos.graph.entity.dto.CreateRoadmapRequest;
import com.midzuhara.roadmapos.graph.entity.dto.RoadmapResponseDto;
import com.midzuhara.roadmapos.graph.entity.dto.UpdateRoadmapRequest;
import com.midzuhara.roadmapos.graph.exception.RoadmapNotFoundException;
import com.midzuhara.roadmapos.graph.mapper.RoadmapMapper;
import com.midzuhara.roadmapos.graph.repository.RoadmapRepository;
import com.midzuhara.roadmapos.graph.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapMapper roadmapMapper;


    @Override
    @Transactional
    public RoadmapResponseDto create(CreateRoadmapRequest request) {
        log.info("Creating roadmap with title: {}", request.title());
        Roadmap roadmap = Roadmap.builder()
                .title(request.title())
                .description(request.description())
                .build();
        Roadmap savedRoadmap = roadmapRepository.save(roadmap);
        return roadmapMapper.toResponseDto(savedRoadmap);
    }

    @Override
    @Transactional(readOnly = true)
    public RoadmapResponseDto getById(Long id) {
        log.debug("Getting roadmap with ID: {}", id);
        return roadmapRepository
                .findById(id)
                .map(roadmapMapper::toResponseDto)
                .orElseThrow(() -> new RoadmapNotFoundException("Roadmap not found with ID: " + id));
    }

    @Override
    @Transactional
    public RoadmapResponseDto update(Long id, UpdateRoadmapRequest request) {
        log.info("Updating roadmap with ID: {}", id);
        return roadmapRepository
                .findById(id)
                .map(roadmap -> {
                    roadmap.setTitle(request.title());
                    roadmap.setDescription(request.description());
                    return roadmapMapper.toResponseDto(roadmap);
                })
                .orElseThrow(() -> new RoadmapNotFoundException("Roadmap not found with ID: " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.warn("Deleting roadmap with ID: {}", id);
        if (!roadmapRepository.existsById(id)) {
            throw new RoadmapNotFoundException("Roadmap not found with ID: " + id);
        }
        roadmapRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadmapResponseDto> getAll() {
        log.debug("Getting all roadmaps");
        return roadmapRepository.findAll().stream()
                .map(roadmapMapper::toResponseDto)
                .toList();
    }
}
