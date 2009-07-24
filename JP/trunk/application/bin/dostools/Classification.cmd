@echo off
rem ######################################################################
rem #
rem # This script checks/loads/updates/delete or save the data of classification objects
rem # Use on Windows systems.
rem # usage Classification.cmd -c|-l|-u|-d|-s|-h [ [<file.xml>|<dir>] | <number> ]
rem #######################################################################


set THIS_HOME=%cd%
set WORKING_DIR=../../docportal/content/classification
set prefile=docportal_class
set predir=DocPortal_Classification_Server
set ISDIR=false
set cl= load

for %%a in (. h. H. help. HELP. -h. -H. -help. -HELP.) do if %1.==%%a goto CaseUsage

if %1.==-c. goto CaseCheckClassification
if %1.==-l. goto CaseLoadClassification
if %1.==-d. goto CaseDeleteClassification
if %1.==-u. goto CaseUpdateClassification
if %1.==-s. goto CaseSaveClassification
goto CaseUsage

:CaseCheckClassification
  echo BATCH  Check Classification
  if %2.==. goto CheckClassificationDir
  if exist %WORKING_DIR%%2  goto ChangePath
  if not exist %2 goto CaseErr
  goto CheckClassificationFile

  :ChangePath
   echo BATCH  change to directory %WORKING_DIR%
   cd %WORKING_DIR%
   goto CheckClassificationFile
   
  :CheckClassificationFile
   echo BATCH  check the File %2 
   call %THIS_HOME%/../bin/mycore.cmd check file %2
   goto CaseEnd

  :CheckClassificationDir
   cd %WORKING_DIR%
   echo BATCH  check all Files from Directory  %cd%
   for %%F IN (*.xml) do   call %THIS_HOME%/../bin/mycore.cmd check file %%F
   goto CaseEnd
  
:CaseUpdateClassification
  set cl= update 

:CaseLoadClassification
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
  call %THIS_HOME%/../bin/mycore.cmd  %cl% classification from file %WORKING_DIR%%2
  goto CaseEnd
  
:Dir
   set WORKING_DIR=%2  
   goto LoadOrUpdateDir

:LoadOrUpdateDir
  set command=%cl% all classifications from directory  %WORKING_DIR%
  echo BATCH  %command% 
  call %THIS_HOME%/../bin/mycore.cmd %command%
  goto CaseEnd
    

:CaseDeleteClassification
  echo BATCH delete Classification
  call %THIS_HOME%/../bin/mycore.cmd delete classification %prefile%_%2
  goto CaseEnd

:CaseSaveClassification
   echo BATCH  Save Docportal-Classification-Object to File
   if not exist ../save mkdir ../save
   cd ..\save
   if not exist %predir% mkdir %predir%
   set f1=%prefile%_%2
   call %THIS_HOME%/../bin/mycore.cmd save classification %f1% to ../save/%predir%/%f1%
   goto CaseEnd


rem ---------- err and usage and end 
:CaseErr
  echo BATCH  File %2 not found, not exist or empty.
  
:CaseWrongPar
  echo BATCH  Wrong Parameter, see usage 
  
:CaseUsage 
  echo BATCH  usage: "Classification.cmd -c|-l|-u|-d|-s|-h [ [<file.xml>|<dir>] | <number> [<numberTO] ]"
  echo BATCH  ___________________________________________________
  echo BATCH  working directory is either . or %WORKING_DIR%  
  echo BATCH  ___________________________________________________
  echo BATCH  checking example: Classification.cmd -c 
  echo BATCH  checks all classification-xml-files in working directory %WORKING_DIR%  
  echo BATCH  ___________________________________________________
  echo BATCH  loading: Classification.cmd -l ../mycontent/classification
  echo BATCH  loads all classification-xml-files from the given directory 
  echo BATCH  ___________________________________________________
  echo BATCH  update: Classification.cmd -u docportal_classification_0000001.xml
  echo BATCH  updates the data in db from the file
  echo BATCH  ___________________________________________________
  echo BATCH  delete: Classification.cmd -d 1 5
  echo BATCH  deletes all classification-objects from the ID 1 to 5 in db
  echo BATCH  ___________________________________________________
  echo BATCH  save: Classification.cmd -s 1
  echo BATCH  saves the classification-object with ID 1 to file docportal_classification_0000001.xml
  echo BATCH  ___________________________________________________
  echo BATCH  Classification.cmd -h shows this screen  
  goto CaseEnd

:CaseEnd
  cd %THIS_HOME%
  echo BATCH  End Classification
  @echo on
  