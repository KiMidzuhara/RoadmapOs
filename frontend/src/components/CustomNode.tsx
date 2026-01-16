import { Handle, Position, type NodeProps } from '@xyflow/react';
import { Lock, CheckCircle2, PlayCircle } from 'lucide-react';
import { clsx } from 'clsx';
import type { Status } from '../types/api';

// Данные, которые мы храним внутри узла
export type NodeData = {
    label: string;
    status: Status;
    onStatusChange: (status: Status) => void; // Колбек для смены статуса
};

// Мы используем базовый NodeProps и "кастим" (приводим) data внутри.
// Это самый безопасный способ избежать ошибок типов.
export const CustomNode = ({ data, selected }: NodeProps) => {

    // ФИКС: Явно говорим TypeScript, что пришедшая data — это именно наш NodeData
    const nodeData = data as unknown as NodeData;

    // Определяем стили в зависимости от статуса
    const getStatusStyles = () => {
        switch (nodeData.status) {
            case 'COMPLETED':
                return 'border-green-500 bg-green-50 shadow-green-100';
            case 'AVAILABLE':
                return 'border-blue-500 bg-white shadow-blue-100 ring-2 ring-blue-500/20';
            case 'LOCKED':
            default:
                return 'border-slate-300 bg-slate-100 text-slate-400 grayscale';
        }
    };

    const getIcon = () => {
        switch (nodeData.status) {
            case 'COMPLETED': return <CheckCircle2 className="text-green-500" size={20} />;
            case 'AVAILABLE': return <PlayCircle className="text-blue-500" size={20} />;
            case 'LOCKED': return <Lock className="text-slate-400" size={20} />;
            default: return <Lock className="text-slate-400" size={20} />;
        }
    };

    return (
        <div className={clsx(
            "px-4 py-3 rounded-xl border-2 shadow-lg transition-all duration-300 min-w-[150px]",
            getStatusStyles(),
            selected && "scale-105 ring-4 ring-offset-2 ring-blue-400" // Эффект при клике
        )}>
            {/* Точка входа (сверху) */}
            <Handle type="target" position={Position.Top} className="!bg-slate-400 !w-3 !h-3" />

            <div className="flex items-center gap-3">
                {getIcon()}
                {/* Используем nodeData вместо data */}
                <div className="font-bold text-sm select-none">{nodeData.label}</div>
            </div>

            {/* Кнопка действий (появляется только если AVAILABLE) */}
            {nodeData.status === 'AVAILABLE' && (
                <button
                    onClick={(e) => {
                        e.stopPropagation(); // Чтобы не выделять узел при клике на кнопку
                        nodeData.onStatusChange('COMPLETED');
                    }}
                    className="mt-2 w-full text-xs bg-blue-500 hover:bg-blue-600 text-white py-1 rounded transition"
                >
                    Complete
                </button>
            )}

            {/* Кнопка отмены (если COMPLETED) - для демо */}
            {nodeData.status === 'COMPLETED' && (
                <button
                    onClick={(e) => {
                        e.stopPropagation();
                        nodeData.onStatusChange('AVAILABLE');
                    }}
                    className="mt-2 w-full text-xs text-slate-500 hover:text-red-500 transition"
                >
                    Reset
                </button>
            )}

            {/* Точка выхода (снизу) */}
            <Handle type="source" position={Position.Bottom} className="!bg-slate-400 !w-3 !h-3" />
        </div>
    );
};