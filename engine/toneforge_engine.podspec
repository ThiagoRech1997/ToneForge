# ToneForge engine como pod local consumido pelo flutter_app no iOS.
# O flutter_app/ios/Podfile referencia este pod via :path => '../../engine'.
# Todas as fontes portáveis + o backend CoreAudio ficam no mesmo pod,
# compilados como estático dentro do Runner. Ver docs/ios-build.md.
Pod::Spec.new do |s|
  s.name             = 'toneforge_engine'
  s.version          = '0.1.0'
  s.summary          = 'ToneForge C++ audio engine with iOS CoreAudio backend.'
  s.description      = <<-DESC
    Engine portátil de processamento de áudio do ToneForge. O mesmo código
    C++ roda no Android (backend Oboe) e no iOS (backend AudioUnit/
    RemoteIO). Consumido pelo app Flutter via Dart FFI, e pelo app Android
    legado via JNI shim.
  DESC
  s.homepage         = 'https://github.com/thiagofernendorech/toneforge'
  s.license          = { :type => 'MIT' }
  s.author           = { 'Thiago Fernendorech' => 'noreply@example.com' }
  s.source           = { :path => '.' }

  s.platform         = :ios, '13.0'
  s.ios.deployment_target = '13.0'

  s.source_files = [
    'include/toneforge/*.h',
    'src/audio_engine.cpp',
    'src/recorder.cpp',
    'src/loop_io.cpp',
    'src/wav_writer.h',
    'src/wav_writer.cpp',
    'src/audio_io_coreaudio.mm',
  ]
  s.public_header_files = 'include/toneforge/*.h'
  s.header_mappings_dir = 'include'

  s.frameworks = 'AVFoundation', 'AudioToolbox', 'CoreAudio', 'Foundation'

  s.pod_target_xcconfig = {
    'CLANG_CXX_LANGUAGE_STANDARD' => 'c++17',
    'CLANG_CXX_LIBRARY'           => 'libc++',
    'HEADER_SEARCH_PATHS'         => '$(PODS_TARGET_SRCROOT)/include',
    'GCC_PREPROCESSOR_DEFINITIONS' => 'TONEFORGE_IOS=1',
  }

  # Flutter consome a lib via DynamicLibrary.process() no iOS — os
  # símbolos extern "C" ficam no binário principal. Não precisamos
  # expor nada extra via modulemap.
end
