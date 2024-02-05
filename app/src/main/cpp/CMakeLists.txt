cmake_minimum_required(VERSION 3.18.1)

project("pdos_pdandro")

if (${ANDROID_ABI} STREQUAL "armeabi-v7a" OR ${ANDROID_ABI} STREQUAL "x86")
#if (${ANDROID_ABI} STREQUAL "x86")
add_subdirectory(${CMAKE_SOURCE_DIR}/pdpclib/${ANDROID_ABI})
add_subdirectory(${CMAKE_SOURCE_DIR}/bios/${ANDROID_ABI})
#add_subdirectory(${CMAKE_SOURCE_DIR}/pcomm/${ANDROID_ABI})
endif()

if (${ANDROID_ABI} STREQUAL "arm64-v8a" OR ${ANDROID_ABI} STREQUAL "x86_64")
    add_subdirectory(${CMAKE_SOURCE_DIR}/bios/${ANDROID_ABI})
endif()

enable_language(C)

add_executable(
        libpdos.so

        pdos.c)

add_executable(
        libls.so

        ls.c)


#add_executable(libtest.so
#        IMPORTED
#        )
