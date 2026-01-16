package com.midzuhara.roadmapos.graph.service;

import com.midzuhara.roadmapos.graph.dto.CreateRoadmapRequest;
import com.midzuhara.roadmapos.graph.dto.RoadmapResponseDto;
import com.midzuhara.roadmapos.graph.dto.UpdateRoadmapRequest;
import com.midzuhara.roadmapos.graph.exception.RoadmapNotFoundException;

import java.util.List;

public interface RoadmapService {

    /**
     * Создает новую карту.
     * @param request - данные (название, описание)
     * @return созданная карта (пока пустая, без узлов)
     */
    RoadmapResponseDto create(CreateRoadmapRequest request);

    /**
     * Возвращает полную карту по ID.
     * Включает в себя списки всех Nodes и Edges.
     * @param id - ID карты
     * @return DTO карты
     * @throws RoadmapNotFoundException если карта не найдена
     */
    RoadmapResponseDto getById(Long id);

    /**
     * Возвращает список всех карт (для главной страницы).
     * В MVP можно возвращать полный объект, но в будущем лучше сделать упрощенный DTO.
     */
    List<RoadmapResponseDto> getAll();

    /**
     * Обновляет карту по ID.
     * @param id - ID карты
     * @param request - новые данные (название, описание)
     * @return обновленная карта
     * @throws RoadmapNotFoundException если карта не найдена
     */
    RoadmapResponseDto update(Long id, UpdateRoadmapRequest request);

    /**
     * Удаляет карту по ID.
     * @param id - ID карты
     * @throws RoadmapNotFoundException если карта не найдена
     */
    void delete(Long id);

}
