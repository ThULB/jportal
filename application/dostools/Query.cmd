@echo off
rem ######################################################################
rem #
rem # This script querys data for Classifications, Documents, Institutions or Person objects
rem # Use on Windows systems.
rem # usage Query.cmd -class|-doc|-inst|-pers|-h <number>
rem #######################################################################


set THIS_HOME=%cd%

for %%a in (. h. H. help. HELP. -h. -H. -help. -HELP.) do if %1.==%%a goto CaseUsage

if %1.==-class. goto CaseQeryClass
if %1.==-doc. 	goto CaseQueryDocument
if %1.==-pers. 	goto CaseQueryPerson
if %1.==-inst. 	goto CaseQueryInstitution
goto CaseUsage

:CaseQeryClass
  echo BATCH  query a Classification 
  if %2.==. set c=DocPortal_class_%2
  if %2.==. set c=DocPortal_class_00000001

  set comm="query local class"
  set q="/mycoreclass[@ID = \"%c%\"]"

  goto CaseExec
  
:CaseQueryDocument
  echo BATCH  query a document
  if %2.==. set c=DocPortal_document_%2
  if %2.==. set c=DocPortal_document_00000001
  
  set comm="query local alldocs"
  rem set comm="query local document"
  rem set comm="query local disshab"
  rem set comm="query remote alldocs"
  rem set comm="query host selfremote alldocs"
  
  rem BATCH setting examples for CM8=   eXist=+ !!
  
  rem BATCH check the empty variant
  set q="*"
  rem BATCH check the MCRObject basic attribute data
  rem set   q="/mycoreobject[@ID=\"DocPortal_document_00000003\"]"
  rem set   q="/mycoreobject[@ID=\"UBLDissHabil_disshab_00000010\"]"
  rem set   q="/mycoreobject[@label like \"Foto*\"]"
  rem set   q="/mycoreobject[@label like \"2003-45*\"]"
  
  rem BATCH querying the TITLE
  set q="/mycoreobject[metadata/titles/title/text() contains(\"Alaska\")]"
  
  rem BATCH check the AUTHOR
  rem set   q="/mycoreobject[metadata/creators/creator/text() contains(\"Rahm\")]"
  rem set   q="/mycoreobject[metadata/creatorlinks/creatorlink/@xlink:title like \"Jens\"]"
  rem set   q="/mycoreobject[metadata/creators/creator/text() contains(\"Rahm\")] or /mycoreobject[metadata/creatorlinks/creatorlink/@xlink:title like \"*Rahm*\"]"
  rem set   q="/mycoreobject[metadata/creators/creator/text() contains(\"Jens\")] or /mycoreobject[metadata/creatorlinks/creatorlink/@xlink:title like \"*Jens*\"]"
  
  rem BATCH check the ORIGIN data
  rem set   q="/mycoreobject[metadata/origins/origin[@classid=\"DocPortal_class_00000003\" and @categid like \"Uni.Leipzig.*\"] ]"
  rem BATCH check the fulltext search in the metadata
  rem set   q="/mycoreobject[text() contains(\"Randbereich\")]"
  rem set check the fulltext search
  rem set   q="/mycoreobject[doctext() contains(\"Randbereich\")]"
  goto CaseExec

:CaseQueryPerson
  echo BATCH  query a person
  if %2.==. set c=DocPortal_person_%2
  if %2.==. set c=DocPortal_person_00000001
  goto CaseExec
    
:CaseQueryInstitution
  echo BATCH  query an institution
  if %2.==. set c=DocPortal_institution_%2
  if %2.==. set c=DocPortal_institution_00000001
  goto CaseExec
  
:CaseExec
  echo BATCH call mycore.cmd %comm% %q%  
  call %THIS_HOME%/../bin/mycore.cmd %comm% %q%
  goto CaseEnd

rem ---------- err and usage and end 
:CaseErr
  echo BATCH  File %2 not found, not exist or empty.
  
:CaseWrongPar
  echo BATCH  Wrong Parameter, see usage 
  
:CaseUsage 
  echo BATCH  "usage Query.cmd -class|-doc|-inst|-pers|-h [<number>]"
  echo BATCH  ___________________________________________________
  echo BATCH  querying data objects like Documents, Institutions, Persons and Classifications
  echo BATCH  ___________________________________________________
  echo BATCH  Query.cmd -doc 5 
  echo BATCH  queries the data of docportal_document_0000005.xml
  echo BATCH  Query.cmd -doc  
  echo BATCH  queries the data of docportal_document_0000001.xml
  echo BATCH  ___________________________________________________
  echo BATCH  Query.cmd -h shows this screen  
  goto CaseEnd

:CaseEnd
  cd %THIS_HOME%
  echo BATCH  End Query
  @echo on
  