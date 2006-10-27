@echo off
rem
rem Save the groups /users
rem
rem SaveUser.sh password
rem

set DIR=$INSTALL_PATH\save\user
set PW=%1%
set TMP1=SaveUser.tmp

cd $INSTALL_PATH\bin

if not exist %DIR% 
md %DIR%
echo change to user administrator with %PW% > %TMP1%
echo save all groups to file %DIR%\groups.xml >> %TMP1%
echo save all users to file %DIR%\users.xml >> %TMP1%
echo quit >> %TMP1%

type %TMP1%

call $INSTALL_PATH\bin\mycore.cmd < %TMP1%

del %TMP1%