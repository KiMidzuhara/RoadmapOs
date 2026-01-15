package com.midzuhara.roadmapos.graph.service.serviceImpl;

import com.midzuhara.roadmapos.graph.entity.Node;
import com.midzuhara.roadmapos.graph.entity.Roadmap;
import com.midzuhara.roadmapos.graph.dto.CreateNodeRequest;
import com.midzuhara.roadmapos.graph.dto.NodeDto;
import com.midzuhara.roadmapos.graph.dto.UpdatePositionRequest;
import com.midzuhara.roadmapos.graph.exception.NodeNotFoundException;
import com.midzuhara.roadmapos.graph.exception.RoadmapNotFoundException;
import com.midzuhara.roadmapos.graph.mapper.RoadmapMapper;
import com.midzuhara.roadmapos.graph.repository.EdgeRepository;
import com.midzuhara.roadmapos.graph.repository.NodeRepository;
import com.midzuhara.roadmapos.graph.repository.RoadmapRepository;
import com.midzuhara.roadmapos.graph.service.GraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphEditorService implements GraphService {

    private final RoadmapRepository roadmapRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final RoadmapMapper roadmapMapper;

    @Override
    @Transactional
    public NodeDto addNode(Long roadmapId, CreateNodeRequest request) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new RoadmapNotFoundException("Roadmap not found with ID: " + roadmapId));
        Node node = Node.builder()
                .title(request.title())
                .positionX(request.x())
                .positionY(request.y())
                .roadmap(roadmap)
                .build();
        Node savedNode = nodeRepository.save(node);
        return roadmapMapper.toNodeDto(savedNode);
    }

    @Override
    @Transactional
    public NodeDto updateNodePosition(Long nodeId, UpdatePositionRequest request) {
        Node node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new NodeNotFoundException("Node not found with ID: " + nodeId));
        node.setPositionX(request.x());
        node.setPositionY(request.y());
        Node updatedNode = nodeRepository.save(node);
        return roadmapMapper.toNodeDto(updatedNode);
    }

    @Override
    @Transactional
    public void deleteNode(Long nodeId) {
        if (!nodeRepository.existsById(nodeId)) {
            throw new NodeNotFoundException("Node not found with ID: " + nodeId);
        }
        nodeRepository.deleteById(nodeId);
    }
}
