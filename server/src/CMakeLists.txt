cmake_minimum_required(VERSION 3.27)
project(src)

set(CMAKE_CXX_STANDARD 14)

add_executable(src
        .idea/.gitignore
        .idea/workspace.xml
        actions.cpp
        database.cpp
        main.cpp
        server.cpp)
