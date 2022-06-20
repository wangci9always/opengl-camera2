
cmake_minimum_required(VERSION 3.4.1)
#导入头文件
include_directories(include)

#add_subdirectory 添加一个子目录并构建该目录
add_subdirectory(alignment)
#设置系统环境变量
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}")
message(-------------)
message(${CMAKE_SOURCE_DIR})
add_library(
        native-lib
        SHARED
        native-lib.cpp
        FaceTracker.cpp
)

target_link_libraries(
        native-lib
        seeta_fa_lib
        opencv_java4
        android
        log)
