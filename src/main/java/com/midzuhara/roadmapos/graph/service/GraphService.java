package com.midzuhara.roadmapos.graph.service;

import com.midzuhara.roadmapos.graph.dto.*;

public interface GraphService {

    NodeDto addNode(Long roadmapId, CreateNodeRequest request);

    NodeDto updateNodePosition(Long nodeId, UpdatePositionRequest request);

    void deleteNode(Long nodeId);

    EdgeDto createEdge(Long roadmapId, CreateEdgeRequest request);

    void deleteEdge(Long edgeId);
}
