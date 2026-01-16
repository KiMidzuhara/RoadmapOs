package com.midzuhara.roadmapos.graph.service.serviceImpl;

import com.midzuhara.roadmapos.graph.dto.*;
import com.midzuhara.roadmapos.graph.entity.Edge;
import com.midzuhara.roadmapos.graph.entity.Node;
import com.midzuhara.roadmapos.graph.entity.Roadmap;
import com.midzuhara.roadmapos.graph.entity.Status;
import com.midzuhara.roadmapos.graph.exception.NodeNotFoundException;
import com.midzuhara.roadmapos.graph.exception.RoadmapNotFoundException;
import com.midzuhara.roadmapos.graph.mapper.RoadmapMapper;
import com.midzuhara.roadmapos.graph.repository.EdgeRepository;
import com.midzuhara.roadmapos.graph.repository.NodeRepository;
import com.midzuhara.roadmapos.graph.repository.RoadmapRepository;
import com.midzuhara.roadmapos.graph.service.GraphService;
import com.midzuhara.roadmapos.graph.service.grpc.GrpcValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GraphEditorService implements GraphService {

    private final RoadmapRepository roadmapRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final RoadmapMapper roadmapMapper;
    private final GrpcValidationService grpcValidationService;
    private final NodeStatusService nodeStatusService;

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
                .status(Status.AVAILABLE)
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
    public void batchUpdateNodePositions(BatchUpdatePositionRequest request){
        for (var update : request.updates()) {
            nodeRepository.findById(update.nodeId()).ifPresent(node -> {
                node.setPositionX(update.x());
                node.setPositionY(update.y());
            });
        }
    }

    @Override
    @Transactional
    public void deleteNode(Long nodeId) {
        if (!nodeRepository.existsById(nodeId)) {
            throw new NodeNotFoundException("Node not found with ID: " + nodeId);
        }

        List<Long> childrenIds = edgeRepository
                .findAllBySourceNodeId(nodeId)
                .stream()
                .map(e -> e.getTargetNode().getId())
                .toList();
        // Удаляем все ребра, входящие в этот узел
        edgeRepository.deleteAll(edgeRepository.findAllByTargetNodeId(nodeId));
        // Удаляем все ребра, исходящие из этого узла
        edgeRepository.deleteAll(edgeRepository.findAllBySourceNodeId(nodeId));
        // Удаляем узел
        nodeRepository.deleteById(nodeId);

        for (var childId : childrenIds) {
            nodeRepository.findById(childId).ifPresent(child -> {
                if (child.getStatus() == Status.LOCKED){
                    boolean allParentsCompleted = edgeRepository
                            .findAllByTargetNodeId(child.getId())
                            .stream()
                            .map(Edge::getSourceNode)
                            .allMatch(parent -> parent.getStatus() == Status.COMPLETED);
                    if (allParentsCompleted) {
                        child.setStatus(Status.AVAILABLE);
                        nodeRepository.save(child);
                        log.info("Node {} unlocked because all parents completed", child.getId());
                    }
                }
            });
        }
    }
    @Override
    @Transactional
    public EdgeDto createEdge(Long roadmapId, CreateEdgeRequest request) {
        Long sourceId = request.sourceNodeId();
        Long targetId = request.targetNodeId();

        if (sourceId.equals(targetId)){
            throw new IllegalArgumentException("Self-loops are not allowed");
        }

        Node source = nodeRepository.findById(sourceId)
                .orElseThrow(() -> new NodeNotFoundException("Source node not found: " + sourceId));
        Node target = nodeRepository.findById(targetId)
                .orElseThrow(() -> new NodeNotFoundException("Target node not found: " + targetId));

        if (!source.getRoadmap().getId().equals(roadmapId) || !target.getRoadmap().getId().equals(roadmapId)) {
            throw new IllegalArgumentException("Nodes do not belong to roadmap: " + roadmapId);
        }

        if (edgeRepository.existsBySourceNodeIdAndTargetNodeId(sourceId, targetId)) {
            throw new IllegalArgumentException("Edge already exists");
        }

        if (target.getStatus() == Status.COMPLETED) {
            throw new IllegalArgumentException("Cannot add dependency to a COMPLETED node");
        }

        List<Edge> currentEdges = edgeRepository.findAllByRoadmapId(roadmapId);

        boolean isValid = grpcValidationService.validateGraphCycle(currentEdges, sourceId, targetId);
        if (!isValid) {
            throw new IllegalArgumentException("Cycle detected! This edge creates an infinite loop.");
        }

        Edge edge = Edge.builder()
                .roadmap(source.getRoadmap())
                .sourceNode(source)
                .targetNode(target)
                .build();
        Edge savedEdge = edgeRepository.save(edge);

        if (target.getStatus() == Status.AVAILABLE) {
            if (source.getStatus() != Status.COMPLETED) {
                target.setStatus(Status.LOCKED);
                nodeRepository.save(target);
                log.info("Node {} locked because new dependency {} added", target.getId(), source.getId());
            }
        }
       log.info("Edge created: {} -> {}", sourceId, targetId);
        return roadmapMapper.toEdgeDto(savedEdge);
    }

    @Override
    @Transactional
    public void deleteEdge(Long edgeId) {
        Edge edge = edgeRepository.findById(edgeId)
                .orElseThrow(() -> new IllegalArgumentException("Edge not found with ID: " + edgeId));

        Node targetNode = edge.getTargetNode();
        // Удаляем связь
        edgeRepository.delete(edge);

        edgeRepository.flush();

        if (targetNode.getStatus() == Status.LOCKED) {
            // Ищем оставшихся родителей
            List<Edge> remainingParents = edgeRepository.findAllByTargetNodeId(targetNode.getId());

            // Если родителей нет ИЛИ все оставшиеся родители COMPLETED
            boolean allParentsCompleted = remainingParents.isEmpty() ||
                    remainingParents.stream()
                            .map(Edge::getSourceNode)
                            .allMatch(p -> p.getStatus() == Status.COMPLETED);

            if (allParentsCompleted) {
                targetNode.setStatus(Status.AVAILABLE);
                nodeRepository.save(targetNode); // Тут save нужен, чтобы обновить статус
                log.info("Node {} unlocked because dependency removed", targetNode.getId());
            }
        }

        log.info("Edge deleted: {}", edgeId);
    }

    @Override
    @Transactional
    public NodeDto updateNode(Long nodeId, UpdateNodeRequest request) {

        Node node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new NodeNotFoundException("Node not found with ID: " + nodeId));

        if (request.title() != null && !request.title().isBlank()){
            node.setTitle(request.title());
        }

        if (request.status() != null) {
            Status newStatus = Status.valueOf(request.status());
            if (node.getStatus() != newStatus) {
                nodeStatusService.changeNodeStatus(node, newStatus);
            }
        }
        log.info("Node updated: {}", node.getId());
        return roadmapMapper.toNodeDto(node);
    }
}
