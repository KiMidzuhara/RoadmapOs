package com.midzuhara.roadmapos.graph.service.serviceImpl;

import com.midzuhara.roadmapos.graph.entity.Edge;
import com.midzuhara.roadmapos.graph.entity.Node;
import com.midzuhara.roadmapos.graph.entity.Status;
import com.midzuhara.roadmapos.graph.repository.EdgeRepository;
import com.midzuhara.roadmapos.graph.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NodeStatusService {

    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;

    /**
     * Центральный метод изменения статуса.
     * Проверяет правила и запускает цепную реакцию.
     */

    @Transactional
    public void changeNodeStatus(Node node, Status newStatus) {
        if (newStatus == Status.AVAILABLE || newStatus == Status.COMPLETED) {
            checkParentsCompleted(node);
        }
        node.setStatus(newStatus);

        if (newStatus == Status.COMPLETED) {
            tryUnlockChildren(node);
        }
    }

    private void checkParentsCompleted(Node node){
        List<Edge> incomingEdges = edgeRepository.findAllByTargetNodeId(node.getId());
        for (var edge : incomingEdges) {
            if (edge.getSourceNode().getStatus() != Status.COMPLETED) {
                throw new IllegalArgumentException(
                        "Cannot unlock node. Parent '" +
                                edge.getSourceNode().getTitle() +
                                "' is not COMPLETED."
                );
            }
        }
    }

    private void tryUnlockChildren(Node node){
        // Находим всех детей этого узла
        List<Edge> outgoingEdges = edgeRepository.findAllBySourceNodeId(node.getId());
        for (var edge : outgoingEdges) {
            Node child = edge.getTargetNode();
            if (child.getStatus() != Status.LOCKED) {
                continue;
            }
            // Проверяем, все ли родители этого ребенка завершены
            boolean allParentsCompleted = edgeRepository
                    .findAllByTargetNodeId(child.getId())
                    .stream()
                    .map(Edge::getSourceNode)
                    .allMatch(parent -> parent.getStatus() == Status.COMPLETED);

            if (allParentsCompleted) {
                child.setStatus(Status.AVAILABLE);
                nodeRepository.save(child);
            }
        }
    }
}
