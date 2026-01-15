package com.midzuhara.roadmapos.graph.repository;

import com.midzuhara.roadmapos.graph.entity.Edge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EdgeRepository extends JpaRepository<Edge, Long> {

    boolean existsBySourceNodeIdAndTargetNodeId(Long sourceNodeId, Long targetNodeId);

    List<Edge> findAllByRoadmapId(Long roadmapId);

    List<Edge> findAllBySourceNodeId(Long sourceNodeId);

    List<Edge> findAllByTargetNodeId(Long targetNodeId);
}
