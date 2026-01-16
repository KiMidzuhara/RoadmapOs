package com.midzuhara.roadmapos.graph.repository;

import com.midzuhara.roadmapos.graph.entity.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
}
