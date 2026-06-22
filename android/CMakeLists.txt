project(NitroFusedLocation)
cmake_minimum_required(VERSION 3.9.0)

set(CMAKE_CXX_STANDARD 20)

find_package(fbjni REQUIRED CONFIG)
find_package(ReactAndroid REQUIRED CONFIG)

file(GLOB SOURCES
    "src/main/cpp/*.cpp"
    "../nitrogen/generated/android/c++/*.cpp"
    "../nitrogen/generated/shared/c++/*.cpp"
)

add_library(NitroFusedLocation SHARED ${SOURCES})

# 1. Headers ko include karein
set(NITRO_PREFAB_HEADERS "${CMAKE_CURRENT_SOURCE_DIR}/../node_modules/react-native-nitro-modules/android/build/intermediates/prefab_package/debug/prefab/modules/NitroModules/include")

target_include_directories(NitroFusedLocation
    PRIVATE
    "src/main/cpp"
    "../nitrogen/generated/shared/c++"
    "../nitrogen/generated/android/c++"
    ${NITRO_PREFAB_HEADERS}
)

target_compile_definitions(NitroFusedLocation PRIVATE "NITRO_MODULES_AVAILABLE=1")

find_library(LOG_LIB log)

# 2. NitroModules library (.so) ka exact path dhundhein
# ${CMAKE_ANDROID_ARCH_ABI} will automatically be arm64-v8a, armeabi-v7a, etc.
set(NITRO_LIB_PATH "${CMAKE_CURRENT_SOURCE_DIR}/../node_modules/react-native-nitro-modules/android/build/intermediates/prefab_package/debug/prefab/modules/NitroModules/libs/android.${CMAKE_ANDROID_ARCH_ABI}/libNitroModules.so")

# Import the prebuilt Nitro library
add_library(NitroModules SHARED IMPORTED)
set_target_properties(NitroModules PROPERTIES IMPORTED_LOCATION ${NITRO_LIB_PATH})

# 3. Link everything together STIRCTLY
target_link_libraries(NitroFusedLocation
    ${LOG_LIB}
    android
    fbjni::fbjni
    ReactAndroid::jsi
    NitroModules # Force the linker to use the .so file
)