cmake_minimum_required(VERSION 3.18.0)
project(NitroFusedLocation)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_ANDROID_STL_TYPE c++_shared CACHE STRING "" FORCE)

# 1. PEHLE target banao (yahan autolinking target_sources ke liye ise dhundega)
file(GLOB SOURCES "src/main/cpp/*.cpp")
add_library(NitroFusedLocation SHARED ${SOURCES})

# 2. PHIR include karo (ye file target 'NitroFusedLocation' ko use karegi)
include("${CMAKE_CURRENT_SOURCE_DIR}/../nitrogen/generated/android/NitroFusedLocation+autolinking.cmake")

# 3. Agar autolinking mein definitions/includes ki kami reh jaye, toh yahan add karo
target_include_directories(NitroFusedLocation PUBLIC
    "src/main/cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/../nitrogen/generated/shared/c++"
    "${CMAKE_CURRENT_SOURCE_DIR}/../nitrogen/generated/android/c++"
)