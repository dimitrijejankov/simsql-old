def generate_cmake(outfile_path, vg_function):

    of = open(outfile_path, 'w')

    of.write("""cmake_minimum_required(VERSION 3.5)
project(Test)

FIND_PACKAGE(GSL REQUIRED)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

set(SOURCE_FILES Test%s.cpp)

set(INCLUDE_DIRS ${GSL_INCLUDE_DIRS})
set(LIBS ${LIBS} ${GSL_LIBRARIES})

include_directories(${INCLUDE_DIRS})

add_executable(Test ${SOURCE_FILES})

TARGET_LINK_LIBRARIES(Test ${LIBS})""" % vg_function)
