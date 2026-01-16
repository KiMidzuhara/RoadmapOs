import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type {RoadmapResponseDto} from '../types/api';
import { Link } from 'react-router-dom';
import { Plus, Map, ArrowRight } from 'lucide-react';

export const Dashboard = () => {
    const [roadmaps, setRoadmaps] = useState<RoadmapResponseDto[]>([]);

    useEffect(() => {
        api.get<RoadmapResponseDto[]>('/roadmaps').then(res => setRoadmaps(res.data));
    }, []);

    const createRoadmap = async () => {
        const title = prompt("Enter roadmap name:");
        if (!title) return;
        const res = await api.post('/roadmaps', { title, description: "New roadmap" });
        setRoadmaps([...roadmaps, res.data]);
    };

    return (
        <div className="min-h-screen bg-slate-50 p-10">
            <div className="max-w-6xl mx-auto">
                <div className="flex justify-between items-center mb-10">
                    <div>
                        <h1 className="text-4xl font-black text-slate-800 tracking-tight">RoadmapOS</h1>
                        <p className="text-slate-500 mt-2">Create and track your learning paths</p>
                    </div>
                    <button
                        onClick={createRoadmap}
                        className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-xl font-bold shadow-lg shadow-blue-200 transition-all hover:scale-105"
                    >
                        <Plus size={20} /> Create New Roadmap
                    </button>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {roadmaps.map(map => (
                        <Link to={`/editor/${map.id}`} key={map.id} className="group">
                            <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 hover:shadow-xl hover:-translate-y-1 transition-all duration-300 h-full flex flex-col">
                                <div className="flex items-start justify-between mb-4">
                                    <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
                                        <Map size={28} />
                                    </div>
                                    <ArrowRight className="text-slate-300 group-hover:text-blue-500 group-hover:translate-x-1 transition" />
                                </div>
                                <h3 className="text-xl font-bold text-slate-800 mb-2">{map.title}</h3>
                                <p className="text-slate-500 text-sm flex-grow">{map.description || "No description provided."}</p>
                                <div className="mt-6 pt-4 border-t border-slate-100 text-xs font-medium text-slate-400 flex gap-4">
                                    <span>{map.nodes.length} Nodes</span>
                                    <span>{map.edges.length} Connections</span>
                                </div>
                            </div>
                        </Link>
                    ))}

                    {/* Пустая карточка для создания */}
                    <button onClick={createRoadmap} className="border-2 border-dashed border-slate-300 rounded-2xl flex flex-col items-center justify-center p-6 text-slate-400 hover:border-blue-400 hover:text-blue-500 hover:bg-blue-50/50 transition-all min-h-[200px]">
                        <Plus size={40} className="mb-2 opacity-50" />
                        <span className="font-semibold">Add another roadmap</span>
                    </button>
                </div>
            </div>
        </div>
    );
};