# Flutter Android native build — compila o engine como libtoneforge_engine.so
# e expõe para o Dart carregar via DynamicLibrary.open(). O engine/ é
# compartilhado com o app Android legado, mas aqui é construído em modo
# SHARED para o Flutter consumir diretamente sem JNI shim.
# Ver plano .claude/plans/cuddly-jumping-wolf.md Fase 2.
cmake_minimum_required(VERSION 3.22.1)
project(toneforge_flutter_native LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,common-page-size=0x4000")

# engine/ está fora do projeto Flutter — relativo a este arquivo:
# cpp → main → src → app → android → flutter_app → raiz ToneForge (6 ..)
set(TONEFORGE_ENGINE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../engine)

# Força o engine a ser SHARED (o gradle arguments já passa -DTONEFORGE_ENGINE_SHARED=ON,
# mas deixo redundante aqui para robustez caso alguém rode CMake direto).
set(TONEFORGE_ENGINE_SHARED ON CACHE BOOL "" FORCE)

add_subdirectory(${TONEFORGE_ENGINE_DIR} ${CMAKE_BINARY_DIR}/toneforge_engine_build)
