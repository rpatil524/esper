@echo off

if "%JAVA_HOME%" == "" (
  echo.
  echo JAVA_HOME not set
  goto EOF
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo.
  echo Cannot find java executable, check JAVA_HOME
  goto EOF
)

set LIB=..\..\lib
set LIB_COMPILER=..\..\..\dependencies\compiler

set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;..\terminalsvc-receiver\target\example-terminalsvc-receiver-1.0.jar
set CLASSPATH=%CLASSPATH%;..\terminalsvc-sender\target\example-terminalsvc-sender-1.0.jar
set CLASSPATH=%CLASSPATH%;..\terminalsvc-common\target\example-terminalsvc-common-1.0.jar
set CLASSPATH=%CLASSPATH%;..\lib\jboss-jms-api_1.1_spec-1.0.0.Final.jar
set CLASSPATH=%CLASSPATH%;..\lib\jboss-client.jar
set CLASSPATH=%CLASSPATH%;..\..\..\esper-common-9.0.0.jar
set CLASSPATH=%CLASSPATH%;..\..\..\esper-compiler-9.0.0.jar
set CLASSPATH=%CLASSPATH%;..\..\..\esper-runtime-9.0.0.jar
set CLASSPATH=%CLASSPATH%;%LIB_COMPILER%\slf4j-api-1.7.36.jar
set CLASSPATH=%CLASSPATH%;%LIB_COMPILER%\slf4j-reload4j-1.7.36.jar
set CLASSPATH=%CLASSPATH%;%LIB_COMPILER%\reload4j-1.2.25.jar
set CLASSPATH=%CLASSPATH%;%LIB_COMPILER%\antlr4-runtime-4.13.1.jar
set CLASSPATH=%CLASSPATH%;%LIB_COMPILER%\commons-compiler-3.1.12.jar
set CLASSPATH=%CLASSPATH%;%LIB_COMPILER%\janino-3.1.12.jar
