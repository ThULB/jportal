@echo off

rem
rem Save the data of a derivate
rem
rem SaveDerivate.cmd table prefix type
rem SaveDerivate.cmd MCRXMLTABLE DocPortal derivate
rem

set tab=%1%
set prefix=%2%
set type=%3%
set mypath=%DOCPORTAL_HOME%\save\%prefix%_%type%_Server
if not exist %mypath% md %mypath%

set base=%prefix%_%type%
cd $INSTALL_PATH\build\bin

rem For HSQLDB

set SDTMP1=SaveDerivate1.tmp
set SDTMP2=SaveDerivate2.tmp
set com=select MCRID from %tab% where ( MCRID like '%base%%%' );
echo %com% > %SDTMP1%

set CLASSPATH=%DOCPORTAL_HOME%\build\lib\third-party.jar

%JAVA_HOME%/bin/java -Xmx256m org.hsqldb.util.SqlTool --rcfile sqltool.rc localhost-sa < %SDTMP1% > %SDTMP2%
set SDTMP3=SaveDerivate3.tmp
type %SDTMP2% | gnutools\bin\grep %prefix%_%type% > %SDTMP3%

set SDTMP4=SaveDerivate4.tmp
set SDTMP5=SaveDerivate5.tmp
echo { printf "export derivate %%s to directory %mypath% with save" , $2 ; printf "\n" } > %SDTMP5%
findstr sql %SDTMP3% | gnutools\bin\mawk -f %SDTMP5% > %SDTMP4%
echo { printf "export derivate %%s to directory %mypath% with save" , $1 ; printf "\n" } > %SDTMP5%
findstr /V sql %SDTMP3% | gnutools\bin\mawk -f %SDTMP5% >> %SDTMP4%
echo quit >> %SDTMP4%

type %SDTMP4%
%DOCPORTAL_HOME%\build\bin\mycore.cmd < %SDTMP4%

del %SDTMP1%
del %SDTMP2%
del %SDTMP3%
del %SDTMP4%
del %SDTMP5%
