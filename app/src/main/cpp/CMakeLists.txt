# App-side CMake — apenas o shim JNI. O DSP e o I/O vivem em engine/ como
# biblioteca C++ standalone, consumida via add_subdirectory. Isso prepara
# o terreno para o reuso em iOS e em um eventual binding Dart FFI.
# Ver .claude/plans/cuddly-jumping-wolf.md Fase 1.
cmake_minimum_required(VERSION 3.22.1)
project("toneforge")

# Alinhamento de página de 16 KB para Android 15+
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,common-page-size=0x4000")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Engine portátil (fora de app/src/main/cpp). O caminho relativo a partir
# deste arquivo atravessa cpp → main → src → app → raiz do projeto.
set(TONEFORGE_ENGINE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../engine)
add_subdirectory(${TONEFORGE_ENGINE_DIR} ${CMAKE_BINARY_DIR}/toneforge_engine_build)

add_library(${CMAKE_PROJECT_NAME} SHARED
        native-lib.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
        toneforge_engine
        android
        log)
