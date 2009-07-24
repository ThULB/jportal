@echo off

rem
rem list all saveed documents from the server an from the workflow 
rem  

set L=liste.txt
set SLTMP1=SaveList1.tmp
set SLTMP2=SaveList2.tmp
set SLTMP3=SaveList3.tmp
set SLTMP4=SaveList4.tmp
set SLTMP5=SaveList5.tmp

rem cd %DOCPORTAL_HOME%\build\save

echo { printf("\nUebersicht ueber die Daten aus DocPortal\n========================================\n\n") } > %SLTMP5%
type %SLTMP5%| gnutools\bin\mawk -f %SLTMP5% > %L%

echo { printf("A %%s \n",$1) } > %SLTMP3%

dir %DOCPORTAL_HOME%\save\DocPortal_institution_Server|gnutools\bin\grep .xml|gnutools\bin\wc|gnutools\bin\mawk -f %SLTMP3% > %SLTMP1%
call %DOCPORTAL_HOME%\build\bin\SQLCountHSQLDB MCRXMLTABLE DocPortal_institution|gnutools\bin\mawk -f %SLTMP3% > %SLTMP2%
gnutools\bin\join %SLTMP1% %SLTMP2% > %SLTMP4%
echo { printf("  DocPortal_institution_Server        : in save %%s   in DB %%s\n\n",$2,$3) } > %SLTMP5%
type %SLTMP4% | gnutools\bin\mawk -f %SLTMP5% >> %L%

dir %DOCPORTAL_HOME%\save\DocPortal_author_Server|gnutools\bin\grep .xml|gnutools\bin\wc|gnutools\bin\mawk -f %SLTMP3% > %SLTMP1% 
call %DOCPORTAL_HOME%\build\bin\SQLCountHSQLDB MCRXMLTABLE DocPortal_author|gnutools\bin\mawk -f %SLTMP3% > %SLTMP2% 
gnutools\bin\join %SLTMP1% %SLTMP2% > %SLTMP4% 
echo { printf("  Docportal_author_Server             : in save %%s   in DB %%s\n\n",$2,$3) } > %SLTMP5% 
type %SLTMP4% | gnutools\bin\mawk -f %SLTMP5% >> %L%

dir %DOCPORTAL_HOME%\save\DocPortal_document_Server|gnutools\bin\grep .xml|gnutools\bin\wc|gnutools\bin\mawk -f %SLTMP3% > %SLTMP1% 
call %DOCPORTAL_HOME%\build\bin\SQLCountHSQLDB MCRXMLTABLE DocPortal_document|gnutools\bin\mawk -f %SLTMP3% > %SLTMP2%
gnutools\bin\join %SLTMP1% %SLTMP2% > %SLTMP4% 
echo { printf("  DocPortal_document_Server           : in save %%s   in DB %%s\n\n",$2,$3) } > %SLTMP5% 
type %SLTMP4% | gnutools\bin\mawk -f %SLTMP5% >> %L%

dir %DOCPORTAL_HOME%\save\DocPortal_derivate_Server|gnutools\bin\grep .xml|gnutools\bin\wc|gnutools\bin\mawk -f %SLTMP3% > %SLTMP1% 
call %DOCPORTAL_HOME%\build\bin\SQLCountHSQLDB MCRXMLTABLE DocPortal_derivate|gnutools\bin\mawk -f %SLTMP3% > %SLTMP2%
gnutools\bin\join %SLTMP1% %SLTMP2% > %SLTMP4%
echo { printf("  DocPortal_derivate_Server           : in save %%s   in DB %%s\n\n",$2,$3) } > %SLTMP5%
type %SLTMP4% | gnutools\bin\mawk -f %SLTMP5% >> %L%  

echo { printf("  DocPortal_institution_Workflow      : in save %%s\n\n",$1) } > %SLTMP5% 
dir %DOCPORTAL_HOME%\save\DocPortal_institution_Workflow|gnutools\bin\grep .xml|gnutools\bin\wc|gnutools\bin\mawk -f %SLTMP5% >> %L%

echo { printf("  DocPortal_author_Workflow           : in save %%s\n\n",$1) } > %SLTMP5% 
dir %DOCPORTAL_HOME%\save\DocPortal_author_Workflow|gnutools\bin\grep .xml|gnutools\bin\wc|gnutools\bin\mawk -f %SLTMP5% >> %L%

echo { printf("  DocPortal_document_Workflow         : in save %%s\n\n",$1) } > %SLTMP5%
dir %DOCPORTAL_HOME%\save\DocPortal_document_Workflow|gnutools\bin\grep .xml|gnutools\bin\wc|gnutools\bin\mawk -f %SLTMP5% >> %L%

del %SLTMP1%
del %SLTMP2%
del %SLTMP3%
del %SLTMP4%
del %SLTMP5%  

type %L% 