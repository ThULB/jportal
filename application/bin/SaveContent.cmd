@echo off

rem
rem Save the data of a content
rem
rem SaveContent.cmd table prefix type
rem SaveContent.cmd MCRXMLTABLE DocPortal_document
rem

set tab=%1%
set prefix=%2%
set type=%3%

set mypath=$INSTALL_PATH\save\%prefix%_%type%_Server
if not exist %mypath% md %mypath%

set base=%prefix%_%type%
cd $INSTALL_PATH\bin

rem For HSQLDB

set SCTMP1=SaveContent1.tmp
set SCTMP2=SaveContent2.tmp

if exist %SCTMP1% del %SCTMP1%
if exist %SCTMP2% del %SCTMP2%

set com=select MCRID from %tab% where ( MCRID like '%base%%%' );echo %com% > %SCTMP1%
set CLASSPATH=$INSTALL_PATH\lib\mycore\hsqldb_1_8_0_1.jar

%JAVA_HOME%/bin/java -Xmx256m org.hsqldb.util.SqlTool --rcfile ./sqltool.rc localhost-sa < %SCTMP1% > %SCTMP2%

set SCTMP3=SaveContent3.tmp

if exist %SCTMP3% del %SCTMP3%

type %SCTMP2% | ..\dostools\gnutools\bin\grep %prefix%_%type% > %SCTMP3%

set SCTMP4=SaveContent4.tmp
set SCTMP5=SaveContent5.tmp

if exist %SCTMP4% del %SCTMP4%
if exist %SCTMP5% del %SCTMP5%

echo { printf "save object of %%s to directory %mypath%" , $2 ; printf "\n" } > %SCTMP5%
findstr sql %SCTMP3% | ..\dostools\gnutools\bin\mawk -f %SCTMP5% > %SCTMP4%echo { printf "save object of %%s to directory %mypath%" , $1 ; printf "\n" } > %SCTMP5%
findstr /V sql %SCTMP3% | ..\dostools\gnutools\bin\mawk -f %SCTMP5% >> %SCTMP4%
echo quit >> %SCTMP4%

type %SCTMP4%

call $INSTALL_PATH\bin\mycore.cmd < %SCTMP4%del %SCTMP1%del %SCTMP2%del %SCTMP3%del %SCTMP4%del %SCTMP5%