cmake_minimum_required(VERSION 3.30)
project(server)

set(CMAKE_CXX_STANDARD 20)

include_directories(${PROJECT_SOURCE_DIR}/include)
add_executable(server
        src/main.cpp
        src/routes.cpp
        src/database.cpp
)

find_package(SQLite3 REQUIRED)

include_directories(/usr/local/include)
include_directories(/usr/include)

target_link_libraries(server PRIVATE sqlite3 pthread)