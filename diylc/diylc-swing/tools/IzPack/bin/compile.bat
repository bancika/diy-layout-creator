@echo off

REM  Copyright 2001,2004-2005 The Apache Software Foundation
REM
REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem %~dp0 is expanded pathname of the current script
set DEFAULT_IZPACK_HOME=%~dp0..

if "%IZPACK_HOME%"=="" set IZPACK_HOME=%DEFAULT_IZPACK_HOME%
set DEFAULT_IZPACK_HOME=

echo IZPACK_HOME is "%IZPACK_HOME%"

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set IZPACK_CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
set IZPACK_CMD_LINE_ARGS=%IZPACK_CMD_LINE_ARGS% %1
shift
goto setupArgs
rem This label provides a place for the argument list loop to break out
rem and for NT handling to skip to.

:doneStart 
rem find IZPACK_HOME if it does not exist due to either
rem an invalid value passed by the user or the %0 problem 
rem on Windows 9x
set IZPACK_JAR=lib\izpack-compiler-*.jar
if exist "%IZPACK_HOME%\%IZPACK_JAR%" goto checkJava

rem check for Izpack in Program Files
if not exist "%ProgramFiles%\IzPack" goto checkSystemDrive
set IZPACK_HOME=%ProgramFiles%\Izpack
if exist "%IZPACK_HOME%\%IZPACK_JAR%" goto checkJava

:checkSystemDrive
rem check for Izpack in root directory of system drive
if not exist %SystemDrive%\Izpack\%IZPACK_JAR% goto checkCDrive
set IZPACK_HOME=%SystemDrive%\Izpack\
goto checkJava

:checkCDrive
rem check for Izpack in C:\Izpack for Win9X users
if not exist C:\Izpack\%IZPACK_JAR% goto noIzpackHome
set IZPACK_HOME=C:\Izpack\
goto checkJava

:noIzpackHome
echo IZPACK_HOME is set incorrectly or Izpack could not be located. Please set IZPACK_HOME.
goto end

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto setClasspath

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:setClasspath
set LOCALCLASSPATH=%IZPACK_HOME%\Izpack.jar;%CLASSPATH%
for %%i in ("%IZPACK_HOME%\lib\*.jar") do call "%IZPACK_HOME%\bin\lcp.bat" %%i

:runIzpack
set MAIN_CLASS=com.izforge.izpack.compiler.bootstrap.CompilerLauncher

"%_JAVACMD%" -Xmx512m %IZPACK_OPTS% -classpath "%LOCALCLASSPATH%" %MAIN_CLASS% %IZPACK_CMD_LINE_ARGS%
goto end

:end
set _JAVACMD=
set IZPACK_CMD_LINE_ARGS=
set LOCALCLASSPATH=
set MAIN_CLASS=

if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal

:mainEnd

