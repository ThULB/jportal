@echo off

rem
rem Save the groups /users
rem
rem SaveUser.sh password
rem

set DIR=..\..\save\user
set PW=%1%
set TMP1=SaveUser.tmp

cd %DOCPORTAL_HOME%\build\bin

md %DIR%
echo change to user administrator with %PW% > %TMP1%
echo save all groups to file %DIR%\groups.xml >> %TMP1%
echo save all users to file %DIR%\users.xml >> %TMP1%
echo save all permissions to file $DIR/permissions.xml >> %TMP1%
echo quit >> %TMP1%

type %TMP1%

call %DOCPORTAL_HOME%\build\bin\mycore.cmd < %TMP1%

del %TMP1%