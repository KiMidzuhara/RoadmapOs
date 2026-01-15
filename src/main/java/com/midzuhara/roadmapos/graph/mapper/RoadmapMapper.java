package com.midzuhara.roadmapos.graph.mapper;

import com.midzuhara.roadmapos.graph.entity.Edge;
import com.midzuhara.roadmapos.graph.entity.Node;
import com.midzuhara.roadmapos.graph.entity.Roadmap;
import com.midzuhara.roadmapos.graph.entity.dto.EdgeDto;
import com.midzuhara.roadmapos.graph.entity.dto.NodeDto;
import com.midzuhara.roadmapos.graph.entity.dto.RoadmapResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoadmapMapper {

    RoadmapResponseDto toResponseDto(Roadmap roadmap);

    NodeDto toNodeDto(Node node);

    @Mapping(target = "source", source = "sourceNode.id")
    @Mapping(target = "target", source = "targetNode.id")
    EdgeDto toEdgeDto(Edge edge);
}
