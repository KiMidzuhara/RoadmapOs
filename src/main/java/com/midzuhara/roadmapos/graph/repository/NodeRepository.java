package com.midzuhara.roadmapos.graph.repository;

import com.midzuhara.roadmapos.graph.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<Node, Long> {
}
