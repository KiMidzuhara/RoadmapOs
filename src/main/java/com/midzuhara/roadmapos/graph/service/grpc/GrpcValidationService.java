package com.midzuhara.roadmapos.graph.service.grpc;

import com.midzuhara.roadmapos.graph.entity.Edge;
import com.midzuhara.roadmapos.grpc.EdgePair;
import com.midzuhara.roadmapos.grpc.GraphRequest;
import com.midzuhara.roadmapos.grpc.GraphValidatorServiceGrpc;
import com.midzuhara.roadmapos.grpc.ValidationResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GrpcValidationService {

    @GrpcClient("validator-client")
    private GraphValidatorServiceGrpc.GraphValidatorServiceBlockingStub blockingStub;

    /**
     * Отправляет граф в C++ на проверку циклов.
     * @param existingEdges - уже существующие связи в БД
     * @param newSourceId - ID начала новой связи
     * @param newTargetId - ID конца новой связи
     * @return true, если циклов нет. False, если цикл найден.
     */

    public boolean validateGraphCycle(
            List<Edge> existingEdges,
            long newSourceId,
            long newTargetId
    ) {
        // 1. Собираем данные в запрос
       GraphRequest.Builder requestBuilder = GraphRequest.newBuilder();

       for (var edge : existingEdges) {
           requestBuilder.addEdges(EdgePair.newBuilder()
                   .setSourceId(edge.getSourceNode().getId())
                   .setTargetId(edge.getTargetNode().getId())
                   .build());
       }

       requestBuilder.addEdges(EdgePair.newBuilder()
               .setSourceId(newSourceId)
               .setTargetId(newTargetId)
               .build());

        // 2. Делаем удаленный вызов (RPC)
        // Это синхронный вызов, Java будет ждать ответа от C++
       try {
           ValidationResponse response = blockingStub.validateGraph(requestBuilder.build());
           return response.getIsValid();
       } catch (Exception e) {
           // Если C++ сервер лежит, мы не можем гарантировать целостность.
           // Для MVP лучше упасть с ошибкой, чем разрешить цикл.
           throw new RuntimeException("C++ Validator Service is unavailable: " + e.getMessage());
       }
    }
}
