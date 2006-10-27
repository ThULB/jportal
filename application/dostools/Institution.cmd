@echo off
rem ######################################################################
rem #
rem # This script checks/loads/updates/delete or save the data of institution objects
rem # Use on Windows systems.
rem # usage Institution.cmd -c|-l|-u|-d|-s|-h [ [<file.xml>|<dir>] | <number> [<numberTO] ]
rem #######################################################################


set THIS_HOME=%cd%
set WORKING_DIR=../content/institution/
set prefile=docportal_institution
set prefix=DocPortal_institution
set predir=DocPortal_Institution_Server
set ISDIR=false
set cl= load

for %%a in (. h. H. help. HELP. -h. -H. -help. -HELP.) do if %1.==%%a goto CaseUsage

if %1.==-c. goto CaseCheckInstitution
if %1.==-l. goto CaseLoadInstitution
if %1.==-d. goto CaseDeleteInstitution
if %1.==-u. goto CaseUpdateInstitution
if %1.==-s. goto CaseSaveInstitution
goto CaseUsage

:CaseCheckInstitution
  echo BATCH  Check Institution
  if %2.==. goto CheckInstitutionDir
  if exist %WORKING_DIR%%2  goto ChangePath
  if not exist %2 goto CaseErr
  goto CheckInstitutionFile

  :ChangePath
   echo BATCH  change to directory %WORKING_DIR%
   cd %WORKING_DIR%
   goto CheckInstitutionFile
   
  :CheckInstitutionFile
   echo BATCH  check the File %2 
   call %THIS_HOME%/../bin/mycore.cmd check file %2
   goto CaseEnd

  :CheckInstitutionDir
   cd %WORKING_DIR%
   echo BATCH  check all Files from Directory  %cd%
   for %%F IN (*.xml) do   call %THIS_HOME%/../bin/mycore.cmd check file %%F
   goto CaseEnd
  

:CaseLoadInstitution
  echo BATCH  %cl% Institution
  if %2.==. goto LoadOrUpdateDir
  goto FileOrDir
  
:CaseUpdateInstitution
  cl= update 
  echo BATCH  %cl% Institution
  if %2.==. goto LoadOrUpdateDir
  goto FileOrDir

:FileOrDir
  for %%a in (%2/*.xml) do set ISDIR=true
  if %ISDIR%==true   goto Dir
  if %ISDIR%==false  if exist %2 goto File
  goto CaseErr
    
:File  
  if exist %2 set WORKING_DIR=
  if not exist  %WORKING_DIR%%2 goto CaseErr
  echo BATCH %cl% object from file %2 
  call %THIS_HOME%/../bin/mycore.cmd  %cl% object from file %WORKING_DIR%%2
  goto CaseEnd
  
:Dir
   set WORKING_DIR=%2  
   goto LoadOrUpdateDir

:LoadOrUpdateDir
  echo BATCH  %cl% all objects from directory  %WORKING_DIR%
  call %THIS_HOME%/../bin/mycore.cmd %cl% all objects from directory %WORKING_DIR%
  goto CaseEnd
    

:CaseDeleteInstitution
  echo BATCH delete Institution
  if not %3.==. goto CaseFromToDelete

 :CaseDelete
   call %THIS_HOME%/../bin/mycore.cmd delete object %prefile%_%2
   goto CaseEnd

 :CaseFromToDelete
   call %THIS_HOME%/../bin/mycore.cmd delete object from %prefix%_%2 to %prefix%_%3 
   goto CaseEnd

:CaseSaveInstitution
   echo BATCH  Save Docportal-Institution-Object to File
   if not exist ../save mkdir ../save
   if not exist ../save/%predir% mkdir ../save/%predir%
   set f1=%prefile%_%2
   set f2=%prefile%_%3
   if not %3.==. goto CaseFromToSave
   
   :CaseSave
    call %THIS_HOME%/../bin/mycore.cmd save object of %f1% to directory ../save/%predir%
    goto CaseEnd
    
   :CaseFromToSave
    call %THIS_HOME%/../bin/mycore.cmd save object from %f1% to %f2% to directory ../save/%predir%
    goto CaseEnd


rem ---------- err and usage and end 
:CaseErr
  echo BATCH  File %2 not found, not exist or empty.
  
:CaseWrongPar
  echo BATCH  Wrong Parameter, see usage 
  
:CaseUsage 
  echo BATCH  usage: "Institution.cmd -c|-l|-u|-d|-s|-h [ [<file.xml>|<dir>] | <number> [<numberTO] ]"
  echo BATCH  ___________________________________________________
  echo BATCH  working directory is either . or %WORKING_DIR%  
  echo BATCH  ___________________________________________________
  echo BATCH  checking example: Institution.cmd -c 
  echo BATCH  checks all institution-xml-files in working directory %WORKING_DIR%  
  echo BATCH  ___________________________________________________
  echo BATCH  loading: Institution.cmd -l ../mycontent/institution
  echo BATCH  loads all institution-xml-files from the given directory 
  echo BATCH  ___________________________________________________
  echo BATCH  update: Institution.cmd -u docportal_institution_0000001.xml
  echo BATCH  updates the data in db from the file
  echo BATCH  ___________________________________________________
  echo BATCH  delete: Institution.cmd -d 1 5
  echo BATCH  deletes all institution-objects from the ID 1 to 5 in db
  echo BATCH  ___________________________________________________
  echo BATCH  save: Institution.cmd -s 1
  echo BATCH  saves the institution-object with ID 1 to file docportal_institution_0000001.xml
  echo BATCH  ___________________________________________________
  echo BATCH  Institution.cmd -h shows this screen  
  goto CaseEnd

:CaseEnd
  cd %THIS_HOME%
  echo BATCH  End Institution
  @echo on
  