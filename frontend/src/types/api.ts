export type Status = 'LOCKED' | 'AVAILABLE' | 'COMPLETED';

export interface NodeDto {
    id: number;
    title: string;
    positionX: number;
    positionY: number;
    status: Status;
}

export interface EdgeDto {
    id: number;
    source: string;
    target: string;
}

export interface RoadmapResponseDto {
    id: number;
    title: string;
    description: string;
    nodes: NodeDto[];
    edges: EdgeDto[];
}

export interface CreateNodeRequest {
    title: string;
    x: number;
    y: number;
}

export interface CreateEdgeRequest {
    sourceNodeId: number;
    targetNodeId: number;
}

export interface BatchUpdatePositionRequest {
    updates: { nodeId: number; x: number; y: number }[];
}

export interface UpdateNodeRequest {
    title?: string;
    status?: Status;
}