import { useCallback, useEffect, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    ReactFlow,
    Background,
    Controls,
    useNodesState,
    useEdgesState,
    addEdge,
    type Connection,
    type Edge,
    type Node,
    BackgroundVariant,
    ReactFlowProvider
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

import { api } from '../api/client';
import { CustomNode } from '../components/CustomNode';
import { ArrowLeft, Plus } from 'lucide-react';
import type { Status } from '../types/api';

// Обертка для доступа к хукам React Flow
const EditorContent = () => {
    const { id: roadmapId } = useParams();
    const navigate = useNavigate();

    // ФИКС: Явно указываем типы <Node> и <Edge>, чтобы TS не ругался на concat/addEdge
    const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);

    // Регистрируем наш красивый узел
    const nodeTypes = useMemo(() => ({ custom: CustomNode }), []);

    // 1. Загрузка данных
    const fetchGraph = useCallback(async () => {
        try {
            const res = await api.get(`/roadmaps/${roadmapId}`);
            const data = res.data;

            // Превращаем DTO в формат React Flow
            const flowNodes: Node[] = data.nodes.map((n: any) => ({
                id: n.id.toString(),
                type: 'custom',
                position: { x: n.positionX, y: n.positionY },
                data: {
                    label: n.title,
                    status: n.status,
                    onStatusChange: (s: Status) => handleStatusChange(n.id, s)
                },
            }));

            const flowEdges: Edge[] = data.edges.map((e: any) => ({
                id: e.id.toString(),
                source: e.source,
                target: e.target,
                animated: true,
                style: { stroke: '#94a3b8' }
            }));

            setNodes(flowNodes);
            setEdges(flowEdges);
        } catch (e) {
            console.error(e);
            alert("Roadmap not found");
            navigate('/');
        }
    }, [roadmapId, navigate, setNodes, setEdges]); // Добавил зависимости для useCallback

    useEffect(() => { fetchGraph(); }, [fetchGraph]);

    // 2. Создание связи (Connect)
    const onConnect = useCallback(async (params: Connection) => {
        try {
            // Вызываем C++ валидацию через Java
            const res = await api.post(`/roadmaps/${roadmapId}/edges`, {
                sourceNodeId: Number(params.source),
                targetNodeId: Number(params.target)
            });

            // Если успех - рисуем связь
            // ФИКС: TS теперь понимает типы благодаря useEdgesState<Edge>
            setEdges((eds) => addEdge({ ...params, id: res.data.id.toString(), animated: true }, eds));

            // Перезагружаем граф, чтобы обновить статусы (LOCKED логика)
            fetchGraph();
        } catch (e: any) {
            // Безопасное получение сообщения об ошибке
            const msg = e.response?.data?.message || "Cycle detected or invalid edge!";
            alert(msg);
        }
    }, [roadmapId, fetchGraph, setEdges]);

    // 4. Смена статуса (Вынес выше, чтобы использовать в createNode)
    const handleStatusChange = async (nodeId: number, newStatus: Status) => {
        try {
            await api.patch(`/nodes/${nodeId}`, { status: newStatus });
            fetchGraph();
        } catch (e: any) {
            const msg = e.response?.data?.message || "Cannot change status";
            alert(msg);
        }
    };

    // 3. Создание узла
    const createNode = async () => {
        const title = prompt("Enter node title:");
        if (!title) return;

        // Ставим узел в центр экрана (примерно)
        const pos = { x: Math.random() * 400, y: Math.random() * 400 };

        try {
            const res = await api.post(`/roadmaps/${roadmapId}/nodes`, {
                title,
                x: pos.x,
                y: pos.y
            });
            const newNode = res.data;

            const newFlowNode: Node = {
                id: newNode.id.toString(),
                type: 'custom',
                position: { x: newNode.positionX, y: newNode.positionY },
                data: {
                    label: newNode.title,
                    status: newNode.status,
                    onStatusChange: (s: Status) => handleStatusChange(newNode.id, s)
                }
            };

            setNodes((nds) => nds.concat(newFlowNode));
        } catch (e) {
            alert("Error creating node");
        }
    };

    // 5. Сохранение позиции при остановке перетаскивания
    const onNodeDragStop = useCallback(async (_: any, node: Node) => {
        try {
            await api.put(`/nodes/${node.id}/position`, {
                x: node.position.x,
                y: node.position.y
            });
        } catch (e) {
            console.error("Failed to save position");
        }
    }, []);

    // 6. Удаление (Backspace)
    const onNodesDelete = useCallback(async (nodesToDelete: Node[]) => {
        for (const node of nodesToDelete) {
            await api.delete(`/nodes/${node.id}`);
        }
        fetchGraph();
    }, [fetchGraph]);

    const onEdgesDelete = useCallback(async (edgesToDelete: Edge[]) => {
        for (const edge of edgesToDelete) {
            await api.delete(`/edges/${edge.id}`);
        }
        fetchGraph();
    }, [fetchGraph]);

    return (
        <div className="h-screen w-screen flex flex-col">
            {/* Header */}
            <div className="h-16 border-b bg-white flex items-center px-6 justify-between z-10 shadow-sm">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate('/')} className="p-2 hover:bg-slate-100 rounded-full">
                        <ArrowLeft className="text-slate-600" />
                    </button>
                    <h1 className="font-bold text-lg text-slate-800">Editor Mode</h1>
                </div>
                <div className="flex gap-3">
                    <div className="flex items-center gap-2 mr-4 text-sm text-slate-500">
                        <div className="w-3 h-3 rounded-full bg-blue-500"></div> Available
                        <div className="w-3 h-3 rounded-full bg-green-500 ml-2"></div> Completed
                        <div className="w-3 h-3 rounded-full bg-slate-300 ml-2"></div> Locked
                    </div>
                    <button
                        onClick={createNode}
                        className="flex items-center gap-2 bg-slate-900 text-white px-4 py-2 rounded-lg hover:bg-slate-800 transition"
                    >
                        <Plus size={18} /> Add Node
                    </button>
                </div>
            </div>

            {/* Graph Canvas */}
            <div className="flex-grow bg-slate-50">
                <ReactFlow
                    nodes={nodes}
                    edges={edges}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={onConnect}
                    onNodeDragStop={onNodeDragStop}
                    onNodesDelete={onNodesDelete}
                    onEdgesDelete={onEdgesDelete}
                    nodeTypes={nodeTypes}
                    fitView
                    className="bg-slate-50"
                >
                    <Background color="#cbd5e1" variant={BackgroundVariant.Dots} />
                    <Controls className="bg-white shadow-xl border-none" />
                </ReactFlow>
            </div>
        </div>
    );
};

export const Editor = () => (
    <ReactFlowProvider>
        <EditorContent />
    </ReactFlowProvider>
);