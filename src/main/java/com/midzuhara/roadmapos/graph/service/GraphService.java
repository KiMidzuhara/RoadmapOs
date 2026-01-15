package com.midzuhara.roadmapos.graph.service;

import com.midzuhara.roadmapos.graph.entity.dto.CreateNodeRequest;
import com.midzuhara.roadmapos.graph.entity.dto.NodeDto;
import com.midzuhara.roadmapos.graph.entity.dto.UpdatePositionRequest;

public interface GraphService {

    NodeDto addNode(Long roadmapId, CreateNodeRequest request);

    NodeDto updateNodePosition(Long nodeId, UpdatePositionRequest request);

    void deleteNode(Long nodeId);
}
