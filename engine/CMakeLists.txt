# ToneForge engine — biblioteca C++17 standalone, cross-platform.
# Hospeda o DSP (audio_engine) e os backends de I/O (Oboe no Android,
# AVAudioEngine/CoreAudio no iOS futuramente). Consumida pelo projeto
# Android em app/src/main/cpp via add_subdirectory e, em Fase 2+, por um
# projeto Flutter via Dart FFI. Ver .claude/plans/cuddly-jumping-wolf.md
# Fase 1.
cmake_minimum_required(VERSION 3.22.1)
project(toneforge_engine LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(TONEFORGE_ENGINE_SOURCES
    src/audio_engine.cpp
    src/recorder.cpp
    src/loop_io.cpp
    src/wav_writer.cpp
)

# Backends de I/O — seleção por plataforma. Cada backend é compilado só
# na sua plataforma-alvo; o DSP (audio_engine.cpp) é sempre incluído.
#
# Para iOS, o build real é feito via CocoaPods (engine/toneforge_engine.podspec)
# consumido pelo flutter_app/ios/Podfile. Esta branch APPLE existe para
# quem usar um toolchain CMake para iOS/macOS manualmente — o podspec é
# a fonte da verdade oficial na Fase 4.
if(ANDROID)
    list(APPEND TONEFORGE_ENGINE_SOURCES src/audio_io_oboe.cpp)
elseif(APPLE)
    list(APPEND TONEFORGE_ENGINE_SOURCES src/audio_io_coreaudio.mm)
endif()

# Biblioteca SHARED vs STATIC — controlado pelo consumidor.
# - App Android legado (app/src/main/cpp): STATIC, symbols ficam dentro
#   de libtoneforge.so via JNI shim em native-lib.cpp.
# - Flutter via Dart FFI (flutter_app/): SHARED, Dart carrega
#   libtoneforge_engine.so direto via DynamicLibrary.open().
option(TONEFORGE_ENGINE_SHARED "Build toneforge_engine as SHARED library" OFF)
if(TONEFORGE_ENGINE_SHARED)
    add_library(toneforge_engine SHARED ${TONEFORGE_ENGINE_SOURCES})
else()
    add_library(toneforge_engine STATIC ${TONEFORGE_ENGINE_SOURCES})
endif()

target_include_directories(toneforge_engine
    PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
)

if(ANDROID)
    find_package(oboe REQUIRED CONFIG)
    target_link_libraries(toneforge_engine
        PUBLIC
            oboe::oboe
            log
    )
endif()
