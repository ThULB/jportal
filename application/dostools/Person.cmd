@echo off
rem ######################################################################
rem #
rem # This script checks/loads/updates/delete or save the data of person objects
rem # Use on Windows systems.
rem # usage Person.cmd -c|-l|-u|-d|-s|-h [ [<file.xml>|<dir>] | <number> [<numberTO] ]
rem #######################################################################


set THIS_HOME=%cd%
set WORKING_DIR=../content/person/
set prefile=docportal_person
set prefix=DocPortal_person
set predir=DocPortal_Person_Server
set ISDIR=false
set cl= load

for %%a in (. h. H. help. HELP. -h. -H. -help. -HELP.) do if %1.==%%a goto CaseUsage

if %1.==-c. goto CaseCheckPerson
if %1.==-l. goto CaseLoadPerson
if %1.==-d. goto CaseDeletePerson
if %1.==-u. goto CaseUpdatePerson
if %1.==-s. goto CaseSavePerson
goto CaseUsage

:CaseCheckPerson
  echo BATCH  Check Person
  if %2.==. goto CheckPersonDir
  if exist %WORKING_DIR%%2  goto ChangePath
  if not exist %2 goto CaseErr
  goto CheckPersonFile

  :ChangePath
   echo BATCH  change to directory %WORKING_DIR%
   cd %WORKING_DIR%
   goto CheckPersonFile
   
  :CheckPersonFile
   echo BATCH  check the File %2 
   call %THIS_HOME%/../bin/mycore.cmd check file %2
   goto CaseEnd

  :CheckPersonDir
   cd %WORKING_DIR%
   echo BATCH  check all Files from Directory  %cd%
   for %%F IN (*.xml) do   call %THIS_HOME%/../bin/mycore.cmd check file %%F
   goto CaseEnd
  

:CaseLoadPerson
  echo BATCH  %cl% Person
  if %2.==. goto LoadOrUpdateDir
  goto FileOrDir
  
:CaseUpdatePerson
  cl= update 
  echo BATCH  %cl% Person
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
    

:CaseDeletePerson
  echo BATCH delete Person
  if not %3.==. goto CaseFromToDelete

 :CaseDelete
   call %THIS_HOME%/../bin/mycore.cmd delete object %prefix%_%2
   goto CaseEnd

 :CaseFromToDelete
   call %THIS_HOME%/../bin/mycore.cmd delete object from %prefix%_%2 to %prefix%_%3 
   goto CaseEnd

:CaseSavePerson
   echo BATCH  Save Docportal-Person-Object to File
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
  echo BATCH  usage: "Person.cmd -c|-l|-u|-d|-s|-h [ [<file.xml>|<dir>] | <number> [<numberTO] ]"
  echo BATCH  ___________________________________________________
  echo BATCH  working directory is either . or %WORKING_DIR%  
  echo BATCH  ___________________________________________________
  echo BATCH  checking example: Person.cmd -c 
  echo BATCH  checks all person-xml-files in working directory %WORKING_DIR%  
  echo BATCH  ___________________________________________________
  echo BATCH  loading: Person.cmd -l ../mycontent/person
  echo BATCH  loads all person-xml-files from the given directory 
  echo BATCH  ___________________________________________________
  echo BATCH  update: Person.cmd -u docportal_person_0000001.xml
  echo BATCH  updates the data in db from the file
  echo BATCH  ___________________________________________________
  echo BATCH  delete: Person.cmd -d 1 5
  echo BATCH  deletes all person-objects from the ID 1 to 5 in db
  echo BATCH  ___________________________________________________
  echo BATCH  save: Person.cmd -s 1
  echo BATCH  saves the person-object with ID 1 to file docportal_person_0000001.xml
  echo BATCH  ___________________________________________________
  echo BATCH  Person.cmd -h shows this screen  
  goto CaseEnd

:CaseEnd
  cd %THIS_HOME%
  echo BATCH  End Person
  @echo on
  