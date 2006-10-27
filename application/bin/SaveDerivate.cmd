@echo off

rem
rem Save the data of a derivate
rem
rem SaveDerivate.cmd table prefix type
rem SaveDerivate.cmd MCRXMLTABLE DocPortal derivate
rem

set tab=%1%set prefix=%2%
set type=%3%
set mypath=$INSTALL_PATH\save\%prefix%_%type%_Server
if not exist %mypath% md %mypath%

set base=%prefix%_%type%
cd $INSTALL_PATH\bin

rem For HSQLDB

set SDTMP1=SaveContent1.tmp
set SDTMP2=SaveContent2.tmp
set com=select MCRID from %tab% where ( MCRID like '%base%%%' );echo %com% > %SDTMP1%
set CLASSPATH=$INSTALL_PATH\lib\mycore\hsqldb_1_8_0_1.jar%JAVA_HOME%/bin/java -Xmx256m org.hsqldb.util.SqlTool --rcfile ./sqltool.rc localhost-sa < %SDTMP1% > %SDTMP2%
set SDTMP3=SaveContent3.tmp
type %SDTMP2% | ..\dostools\gnutools\bin\grep %prefix%_%type% > %SDTMP3%

set SDTMP4=SaveContent4.tmp
set SDTMP5=SaveContent5.tmp
echo { printf "save derivate of %%s to directory %mypath%" , $2 ; printf "\n" } > %SDTMP5%
findstr sql %SDTMP3% | ..\dostools\gnutools\bin\mawk -f %SDTMP5% > %SDTMP4%echo { printf "save derivate of %%s to directory %mypath%" , $1 ; printf "\n" } > %SDTMP5%
findstr /V sql %SDTMP3% | ..\dostools\gnutools\bin\mawk -f %SDTMP5% >> %SDTMP4%
echo quit >> %SDTMP4%

type %SDTMP4%
$INSTALL_PATH\bin\mycore.cmd < %SDTMP4%del %SDTMP1%del %SDTMP2%del %SDTMP3%del %SDTMP4%del %SDTMP5%