@echo off
rem ######################################################################
rem #
rem # This script loads/updates/delete the derivates 
rem # Use on Windows systems.
rem # usage Derivate.cmd -l|-u|-d|-h [ [<file.xml>|<dir>] | <number> [<numberTO] ]
rem #######################################################################


set THIS_HOME=%cd%
set WORKING_DIR=../content/derivate/
set prefile=docportal_derivate
set prefix=DocPortal_derivate
set ISDIR=false
set cl= load

for %%a in (. h. H. help. HELP. -h. -H. -help. -HELP.) do if %1.==%%a goto CaseUsage

if %1.==-l. goto CaseLoadDer
if %1.==-d. goto CaseDeleteDer
if %1.==-u. goto CaseUpdateDer
goto CaseUsage
 

:CaseLoadDer
  echo BATCH  %cl% Derivate
  if %2.==. goto LoadOrUpdateDir
  goto FileOrDir
  
:CaseUpdateDer
  cl= update 
  echo BATCH  %cl% Derivate
  if %2.==. goto LoadOrUpdateDir
  goto FileOrDir

:FileOrDir
  for %%a in (%2/*.xml) do set ISDIR=true
  if %ISDIR%==true   goto Dir
  if %ISDIR%==false  if exist %2 goto File
  if %ISDIR%==false  if exist  %WORKING_DIR%%2 goto File
  
  goto CaseErr
    
:File  
  if exist %2 set WORKING_DIR=
  if not exist  %WORKING_DIR%%2 goto CaseErr
  echo BATCH %cl% derivate from file %2 
  call %THIS_HOME%/../bin/mycore.cmd  %cl% derivate from file %WORKING_DIR%%2
  goto CaseEnd
  
:Dir
   set WORKING_DIR=%2  
   goto LoadOrUpdateDir

:LoadOrUpdateDir
  echo BATCH  %cl% all derivates from directory  %WORKING_DIR%
  call %THIS_HOME%/../bin/mycore.cmd %cl% all derivates from directory %WORKING_DIR%
  goto CaseEnd
    

:CaseDeleteDer
  echo BATCH delete Derivate
  if not %3.==. goto CaseFromToDelete

 :CaseDelete
   call %THIS_HOME%/../bin/mycore.cmd delete derivate %prefix%_%2
   goto CaseEnd

 :CaseFromToDelete
   call %THIS_HOME%/../bin/mycore.cmd delete derivate from %prefix%_%2 to %prefix%_%3 
   goto CaseEnd


rem ---------- err and usage and end 
:CaseErr
  echo BATCH  File %2 not found, not exist, empty or invalid number.
  
:CaseWrongPar
  echo BATCH  Wrong Parameter, see usage 
  
:CaseUsage 
  echo BATCH  usage: "Derivate.cmd -l|-u|-d|-h [ [<file.xml>|<dir>] | <number> [<numberTO] ]"
  echo BATCH  ___________________________________________________
  echo BATCH  working directory is either . or %WORKING_DIR%  
  echo BATCH  ___________________________________________________
  echo BATCH  loading: Derivate.cmd -l ../mycontent/derivates
  echo BATCH  loads all xml-files from the given directory 
  echo BATCH  ___________________________________________________
  echo BATCH  update: Derivate.cmd -u docportal_derivate_0000001.xml
  echo BATCH  updates the data in db from the file
  echo BATCH  ___________________________________________________
  echo BATCH  delete: Derivate.cmd -d 1 5
  echo BATCH  deletes all derivates from the ID 1 to 5 in db
  echo BATCH  ___________________________________________________
  echo BATCH  Derivate.cmd -h shows this screen  
  goto CaseEnd

:CaseEnd
  cd %THIS_HOME%
  echo BATCH  End Author
  @echo on
  