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

cd $INSTALL_PATH\save

echo { printf("\nUebersicht ueber die Daten aus DocPortal\n========================================\n\n") } > %SLTMP5%
type %SLTMP5%| ..\dostools\gnutools\bin\mawk -f %SLTMP5% > %L%

echo { printf("A %%s \n",$1) } > %SLTMP3%

dir DocPortal_institution_Server|..\dostools\gnutools\bin\grep .xml|..\dostools\gnutools\bin\wc|..\dostools\gnutools\bin\mawk -f %SLTMP3% > %SLTMP1%
call $INSTALL_PATH\bin\SQLCountHSQLDB MCRXMLTABLE DocPortal_institution|..\dostools\gnutools\bin\mawk -f %SLTMP3% > %SLTMP2%
..\dostools\gnutools\bin\join %SLTMP1% %SLTMP2% > %SLTMP4%
echo { printf("  DocPortal_institution_Server        : in save %%s   in DB %%s\n\n",$2,$3) } > %SLTMP5%
type %SLTMP4% | ..\dostools\gnutools\bin\mawk -f %SLTMP5% >> %L%

dir DocPortal_author_Server|..\dostools\gnutools\bin\grep .xml|..\dostools\gnutools\bin\wc|..\dostools\gnutools\bin\mawk -f %SLTMP3% > %SLTMP1% 
call $INSTALL_PATH\bin\SQLCountHSQLDB MCRXMLTABLE DocPortal_author|..\dostools\gnutools\bin\mawk -f %SLTMP3% > %SLTMP2% 
..\dostools\gnutools\bin\join %SLTMP1% %SLTMP2% > %SLTMP4% 
echo { printf("  Docportal_author_Server             : in save %%s   in DB %%s\n\n",$2,$3) } > %SLTMP5% 
type %SLTMP4% | ..\dostools\gnutools\bin\mawk -f %SLTMP5% >> %L%

dir DocPortal_document_Server|..\dostools\gnutools\bin\grep .xml|..\dostools\gnutools\bin\wc|..\dostools\gnutools\bin\mawk -f %SLTMP3% > %SLTMP1% 
call $INSTALL_PATH\bin\SQLCountHSQLDB MCRXMLTABLE DocPortal_document|..\dostools\gnutools\bin\mawk -f %SLTMP3% > %SLTMP2%
..\dostools\gnutools\bin\join %SLTMP1% %SLTMP2% > %SLTMP4% 
echo { printf("  DocPortal_document_Server           : in save %%s   in DB %%s\n\n",$2,$3) } > %SLTMP5% 
type %SLTMP4% | ..\dostools\gnutools\bin\mawk -f %SLTMP5% >> %L%

dir DocPortal_derivate_Server|..\dostools\gnutools\bin\grep .xml|..\dostools\gnutools\bin\wc|..\dostools\gnutools\bin\mawk -f %SLTMP3% > %SLTMP1% 
call $INSTALL_PATH\bin\SQLCountHSQLDB MCRXMLTABLE DocPortal_derivate|..\dostools\gnutools\bin\mawk -f %SLTMP3% > %SLTMP2%
..\dostools\gnutools\bin\join %SLTMP1% %SLTMP2% > %SLTMP4%
echo { printf("  DocPortal_derivate_Server           : in save %%s   in DB %%s\n\n",$2,$3) } > %SLTMP5%
type %SLTMP4% | ..\dostools\gnutools\bin\mawk -f %SLTMP5% >> %L%  

echo { printf("  DocPortal_institution_Workflow      : in save %%s\n\n",$1) } > %SLTMP5% 
dir DocPortal_institution_Workflow|..\dostools\gnutools\bin\grep .xml|..\dostools\gnutools\bin\wc|..\dostools\gnutools\bin\mawk -f %SLTMP5% >> %L%

echo { printf("  DocPortal_Workflow_author           : in save %%s\n\n",$1) } > %SLTMP5% 
dir DocPortal_author_Workflow|..\dostools\gnutools\bin\grep .xml|..\dostools\gnutools\bin\wc|..\dostools\gnutools\bin\mawk -f %SLTMP5% >> %L%

echo { printf("  DocPortal_document_Workflow         : in save %%s\n\n",$1) } > %SLTMP5%
dir DocPortal_document_Workflow|..\dostools\gnutools\bin\grep .xml|..\dostools\gnutools\bin\wc|..\dostools\gnutools\bin\mawk -f %SLTMP5% >> %L%

del %SLTMP1%
del %SLTMP2%
del %SLTMP3%
del %SLTMP4%
del %SLTMP5%  

type %L% 