cmake_minimum_required(VERSION 3.18.1)

project("pdos_pdandro")
enable_language(C)

add_executable(libbios.so
        nothing.c)

add_custom_command(TARGET libbios.so POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_if_different
        ${CMAKE_SOURCE_DIR}/bios/${ANDROID_ABI}/libbios.so
        $<TARGET_FILE_DIR:libbios.so>
        )

