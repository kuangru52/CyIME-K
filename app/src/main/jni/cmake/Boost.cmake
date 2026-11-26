# SPDX-FileCopyrightText: 2015 - 2024 Rime community
#
# SPDX-License-Identifier: GPL-3.0-or-later

set(BOOST_VERSION 1.89.0)
set(BOOST_SHA256 "67acec02d0d118b5de9eb441f5fb707b3a1cdd884be00ca24b9a73c995511f74")
set(BOOST_URL "https://github.com/boostorg/boost/releases/download/boost-${BOOST_VERSION}/boost-${BOOST_VERSION}-cmake.tar.xz")

set(BOOST_TARGET_ARCHIVE "${CMAKE_CURRENT_BINARY_DIR}/boost-${BOOST_VERSION}.tar.xz")

# Possible locations where the archive might be cached
set(_possible_archives
    "${BOOST_TARGET_ARCHIVE}"
    "${CMAKE_SOURCE_DIR}/boost-${BOOST_VERSION}.tar.xz"
    "${CMAKE_SOURCE_DIR}/../boost-${BOOST_VERSION}.tar.xz"
)

set(BOOST_VALID_ARCHIVE "")

foreach(_archive IN LISTS _possible_archives)
  if(EXISTS "${_archive}")
    file(SHA256 "${_archive}" _existing_hash)
    if(_existing_hash STREQUAL BOOST_SHA256)
      set(BOOST_VALID_ARCHIVE "${_archive}")
      break()
    else()
      message(WARNING "Found invalid/corrupted archive at ${_archive} (hash: ${_existing_hash}), deleting...")
      file(REMOVE "${_archive}")
    endif()
  endif()
endforeach()

if(NOT BOOST_VALID_ARCHIVE AND NOT EXISTS "${CMAKE_SOURCE_DIR}/boost")
  set(_urls
      "${BOOST_URL}"
      "https://ghproxy.net/${BOOST_URL}"
      "https://mirror.ghproxy.com/${BOOST_URL}"
  )

  foreach(_url IN LISTS _urls)
    message(STATUS "Downloading Boost ${BOOST_VERSION} from ${_url} ......")
    file(DOWNLOAD "${_url}" "${BOOST_TARGET_ARCHIVE}"
         SHOW_PROGRESS STATUS _dl_status)
    list(GET _dl_status 0 _dl_code)
    list(GET _dl_status 1 _dl_msg)

    if(_dl_code EQUAL 0 AND EXISTS "${BOOST_TARGET_ARCHIVE}")
      file(SHA256 "${BOOST_TARGET_ARCHIVE}" _actual_hash)
      if(_actual_hash STREQUAL BOOST_SHA256)
        set(BOOST_VALID_ARCHIVE "${BOOST_TARGET_ARCHIVE}")
        break()
      else()
        message(STATUS "Downloaded archive from ${_url} has mismatched hash (${_actual_hash}), trying next mirror...")
        file(REMOVE "${BOOST_TARGET_ARCHIVE}")
      endif()
    else()
      message(STATUS "Download failed from ${_url}: ${_dl_msg}")
      if(EXISTS "${BOOST_TARGET_ARCHIVE}")
        file(REMOVE "${BOOST_TARGET_ARCHIVE}")
      endif()
    endif()
  endforeach()

  if(NOT BOOST_VALID_ARCHIVE)
    message(STATUS "Online downloads failed, trying local Boost source ...")
    file(REMOVE_RECURSE "${CMAKE_SOURCE_DIR}/boost")
    if(EXISTS "/tmp/boost_${BOOST_VERSION}")
      file(COPY "/tmp/boost_${BOOST_VERSION}/" DESTINATION "${CMAKE_SOURCE_DIR}/boost")
    elseif(EXISTS "/tmp/boost_1_89_0")
      file(COPY "/tmp/boost_1_89_0/" DESTINATION "${CMAKE_SOURCE_DIR}/boost")
    else()
      message(FATAL_ERROR "Cannot download Boost ${BOOST_VERSION} (SHA256: ${BOOST_SHA256}). "
        "Please download boost-${BOOST_VERSION}-cmake.tar.xz from "
        "${BOOST_URL} "
        "and place it in app/ or ${CMAKE_SOURCE_DIR}, "
        "or extract to /tmp/boost_${BOOST_VERSION}/")
    endif()
  else()
    file(REMOVE_RECURSE "${CMAKE_SOURCE_DIR}/boost")
  endif()
endif()

if(NOT EXISTS "${CMAKE_SOURCE_DIR}/boost")
  if(BOOST_VALID_ARCHIVE AND EXISTS "${BOOST_VALID_ARCHIVE}")
    message(STATUS "Extracting Boost ${BOOST_VERSION} from ${BOOST_VALID_ARCHIVE} ......")
    file(ARCHIVE_EXTRACT INPUT "${BOOST_VALID_ARCHIVE}" DESTINATION ${CMAKE_SOURCE_DIR})
    if(EXISTS "${CMAKE_SOURCE_DIR}/boost-${BOOST_VERSION}")
      file(RENAME "${CMAKE_SOURCE_DIR}/boost-${BOOST_VERSION}" "${CMAKE_SOURCE_DIR}/boost")
    endif()
    # Remove non-essential doc files to speed up IDE symbol indexing (keep tools/ for tools/cmake)
    file(REMOVE_RECURSE
         "${CMAKE_SOURCE_DIR}/boost/doc"
         "${CMAKE_SOURCE_DIR}/boost/status"
         "${CMAKE_SOURCE_DIR}/boost/more")
  endif()
endif()

# Header-only Boost configuration: avoid compiling heavy Boost C++ binaries from source
set(__boost_header_only_libs
    algorithm any array assert bind concept_check config container_hash
    conversion core crc detail filesystem foreach function integer io iterator
    lexical_cast move mp11 mpl multi_index numeric_conversion optional
    predef preprocessor range rational regex scope_exit signals2 smart_ptr
    static_assert system throw_exception tti tuple type_index type_traits
    typeof utility uuid variant variant2)

foreach(__boost_lib ${__boost_header_only_libs})
  if(NOT TARGET "Boost::${__boost_lib}")
    add_library("Boost::${__boost_lib}" INTERFACE IMPORTED GLOBAL)
    if(EXISTS "${CMAKE_SOURCE_DIR}/boost/libs/${__boost_lib}/include")
      set_target_properties("Boost::${__boost_lib}" PROPERTIES
        INTERFACE_INCLUDE_DIRECTORIES "${CMAKE_SOURCE_DIR}/boost/libs/${__boost_lib}/include")
    endif()
  endif()
endforeach()

if(NOT TARGET Boost::headers)
  add_library(Boost::headers INTERFACE IMPORTED GLOBAL)
endif()
if(NOT TARGET Boost::boost)
  add_library(Boost::boost INTERFACE IMPORTED GLOBAL)
endif()
