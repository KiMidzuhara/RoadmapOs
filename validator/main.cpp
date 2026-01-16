#include <iostream>
#include <memory>
#include <string>
#include <vector>
#include <map>
#include <grpcpp/grpcpp.h>

// Подключаем сгенерированные файлы.
// Если CLion их не видит (красным), но проект собирается - это баг индексации.
// Правой кнопкой на папку cmake-build-debug -> Mark Directory as -> Generated Sources Root
#include "validator.pb.h"
#include "validator.grpc.pb.h"

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;

// ВНИМАНИЕ: Проверяем неймспейсы
// Твой пакет: com.midzuhara.roadmapos.grpc
// В C++ это будет:
using com::midzuhara::roadmapos::grpc::GraphValidatorService;
using com::midzuhara::roadmapos::grpc::GraphRequest;
using com::midzuhara::roadmapos::grpc::ValidationResponse;
using com::midzuhara::roadmapos::grpc::EdgePair;

// --- АЛГОРИТМ ПОИСКА ЦИКЛОВ ---

// Цвета для DFS:
// 0 - White (не посещен)
// 1 - Gray (в процессе посещения, в стеке) -> ЦИКЛ ЕСЛИ ВСТРЕТИЛИ
// 2 - Black (полностью обработан)
bool hasCycle(int64_t u, std::map<int64_t, std::vector<int64_t>>& adj, std::map<int64_t, int>& visited) {
    visited[u] = 1; // Красим в серый

    for (int64_t v : adj[u]) {
        if (visited[v] == 1) {
            return true; // Нашли ребро в "серую" вершину -> ЦИКЛ
        }
        if (visited[v] == 0) {
            if (hasCycle(v, adj, visited)) return true;
        }
    }

    visited[u] = 2; // Красим в черный
    return false;
}

// --- РЕАЛИЗАЦИЯ gRPC СЕРВИСА ---

class GraphValidatorServiceImpl final : public GraphValidatorService::Service {
    Status ValidateGraph(ServerContext* context, const GraphRequest* request, ValidationResponse* reply) override {

        std::cout << "Received validation request with " << request->edges_size() << " edges." << std::endl;

        // 1. Строим граф (список смежности)
        std::map<int64_t, std::vector<int64_t>> adj;
        std::map<int64_t, int> visited;
        std::vector<int64_t> all_nodes;

        for (const auto& edge : request->edges()) {
            adj[edge.source_id()].push_back(edge.target_id());

            // Запоминаем все уникальные узлы, чтобы потом запустить DFS для каждой компоненты связности
            if (visited.find(edge.source_id()) == visited.end()) {
                visited[edge.source_id()] = 0; // 0 = White
                all_nodes.push_back(edge.source_id());
            }
            if (visited.find(edge.target_id()) == visited.end()) {
                visited[edge.target_id()] = 0;
                all_nodes.push_back(edge.target_id());
            }
        }

        // 2. Запускаем DFS
        bool cycleFound = false;
        for (int64_t node_id : all_nodes) {
            if (visited[node_id] == 0) {
                if (hasCycle(node_id, adj, visited)) {
                    cycleFound = true;
                    break;
                }
            }
        }

        // 3. Формируем ответ
        if (cycleFound) {
            reply->set_is_valid(false);
            reply->set_error_message("Cycle detected in the graph!");
            std::cout << "-> Result: CYCLE DETECTED" << std::endl;
        } else {
            reply->set_is_valid(true);
            std::cout << "-> Result: OK" << std::endl;
        }

        return Status::OK;
    }
};

void RunServer() {
    std::string server_address("0.0.0.0:50051");
    GraphValidatorServiceImpl service;

    ServerBuilder builder;
    // Слушаем порт без SSL (Insecure)
    builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
    // Регистрируем сервис
    builder.RegisterService(&service);

    std::unique_ptr<Server> server(builder.BuildAndStart());
    std::cout << "C++ gRPC Server listening on " << server_address << std::endl;

    server->Wait();
}

int main(int argc, char** argv) {
    RunServer();
    return 0;
}