@echo off

rem
rem save all documents from the server an from the workflow
rem

set PW=alleswirdgut

rem 

rd %DOCPORTAL_HOME%\save /s /Q
md %DOCPORTAL_HOME%\save

rem Save from database

call %DOCPORTAL_HOME%\build\bin\SaveContent.cmd MCRXMLTABLE DocPortal institution
call %DOCPORTAL_HOME%\build\bin\SaveContent.cmd MCRXMLTABLE DocPortal author
call %DOCPORTAL_HOME%\build\bin\SaveContent.cmd MCRXMLTABLE DocPortal document
call %DOCPORTAL_HOME%\build\bin\SaveDerivate.cmd MCRXMLTABLE DocPortal derivate

rem Save from Workflow

md %DOCPORTAL_HOME%\save\DocPortal_institution_Workflow
xcopy %DOCPORTAL_HOME%\data\workflow\institution\DocPortal* %DOCPORTAL_HOME%\save\DocProtal_institution_Workflow /s /h /i

md %DOCPORTAL_HOME%\save\DocPortal_author_Workflow
xcopy %DOCPORTAL_HOME%\data\workflow\author\DocPortal* %DOCPORTAL_HOME%\save\DocPortal_author_Workflow /s /h /i

md %DOCPORTAL_HOME%\save\DocPortal_document_Workflow
xcopy %DOCPORTAL_HOME%\data\workflow\document\DocPortal* %DOCPORTAL_HOME%\save\DocPortal_document_Workflow /s /h /i

rem Save the user

call %DOCPORTAL_HOME%\build\bin\SaveUser.cmd %PW%

rem Save the classification

md %DOCPORTAL_HOME%\save\classification
xcopy %DOCPORTAL_HOME%\content\classification %DOCPORTAL_HOME%\save\classification\ /s /h /i 

rem Print content

cd %DOCPORTAL_HOME%\save\tree /f /a > inhalt.txt
call %DOCPORTAL_HOME%\build\bin\SaveList.cmd

echo Done.