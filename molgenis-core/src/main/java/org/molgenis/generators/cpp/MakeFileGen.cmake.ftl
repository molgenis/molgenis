<#--helper functions-->
<#include "CPPHelper.ftl">

<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
# Date:        ${date}
# 
# generator:   ${generator} ${version}
#
# 
# THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
#

cmake_minimum_required(VERSION 2.8)
SET(EXECNAME MolgenisCPP)

INCLUDE_DIRECTORIES( "." "../../handwritten/cpp" "C:/Program Files/Java/jdk1.6.0_24/include" "C:/Program Files/Java/jdk1.6.0_24/include/win32" )
LINK_DIRECTORIES( "C:/Program Files/Java/jdk1.6.0_24/lib" )
ADD_DEFINITIONS(-Wall)

add_executable(${EXECNAME} 
  main.cpp
  ${UserHome}/handwritten/cpp/MolgenisServlet.cpp
  <#list model.entities as entity>
  ${entity.namespace?replace(".", "/")}/${JavaName(entity)}.cpp;
  </#list>
)

TARGET_LINK_LIBRARIES(${EXECNAME} -ljvm)

#ENABLE_TESTING()
SET(EXECUTABLE "${EXECNAME}.exe")
ADD_TEST(Startup ${EXECUTABLE})
