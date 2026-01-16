cmake_minimum_required(VERSION 4.0)
project(roadmap_validator)

set(CMAKE_CXX_STANDARD 20)

find_package(Protobuf REQUIRED)
find_package(gRPC REQUIRED)

# Ищем плагин
find_program(GRPC_CPP_PLUGIN NAMES grpc_cpp_plugin DOC "Path to grpc_cpp_plugin")
if(NOT GRPC_CPP_PLUGIN)
    message(FATAL_ERROR "grpc_cpp_plugin not found!")
endif()

# === ИСПРАВЛЕНИЕ ПУТЕЙ ===

# Указываем абсолютный путь к папке с прото-файлами
set(PROTO_SRC_DIR "${CMAKE_CURRENT_SOURCE_DIR}/protos")
# Имя файла
set(PROTO_FILE_NAME "validator.proto")
# Полный путь к файлу
set(PROTO_FILE_FULL "${PROTO_SRC_DIR}/${PROTO_FILE_NAME}")

# Папка для генерации
set(GENERATED_DIR "${CMAKE_CURRENT_BINARY_DIR}/generated")
file(MAKE_DIRECTORY ${GENERATED_DIR})

set(PROTO_SRCS "${GENERATED_DIR}/validator.pb.cc")
set(PROTO_HDRS "${GENERATED_DIR}/validator.pb.h")
set(GRPC_SRCS "${GENERATED_DIR}/validator.grpc.pb.cc")
set(GRPC_HDRS "${GENERATED_DIR}/validator.grpc.pb.h")

# Команда генерации (исправленная)
add_custom_command(
        OUTPUT ${PROTO_SRCS} ${PROTO_HDRS} ${GRPC_SRCS} ${GRPC_HDRS}
        COMMAND ${Protobuf_PROTOC_EXECUTABLE}
        ARGS --grpc_out=${GENERATED_DIR}
        --cpp_out=${GENERATED_DIR}
        --plugin=protoc-gen-grpc=${GRPC_CPP_PLUGIN}
        -I "${PROTO_SRC_DIR}"  # <-- Тут указываем папку protos как include path
        "${PROTO_FILE_FULL}"   # <-- Тут полный путь к файлу
        DEPENDS ${PROTO_FILE_FULL}
)

add_executable(roadmap_validator
        main.cpp
        ${PROTO_SRCS}
        ${GRPC_SRCS}
)

target_include_directories(roadmap_validator PUBLIC
        ${GENERATED_DIR}
        ${Protobuf_INCLUDE_DIRS}
)

target_link_libraries(roadmap_validator
        protobuf::libprotobuf
        gRPC::grpc++
)