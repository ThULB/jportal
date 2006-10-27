@echo off

rem
rem save all documents from the server an from the workflow
rem

set PW=alleswirdgut

rem 

rd $INSTALL_PATH\save /s /Q
md $INSTALL_PATH\save

rem Save from database

call $INSTALL_PATH\bin\SaveContent.cmd MCRXMLTABLE DocPortal institution
call $INSTALL_PATH\bin\SaveContent.cmd MCRXMLTABLE DocPortal author
call $INSTALL_PATH\bin\SaveContent.cmd MCRXMLTABLE DocPortal document
call $INSTALL_PATH\bin\SaveDerivate.cmd MCRXMLTABLE DocProtal derivate

rem Save from Workflow

md $INSTALL_PATH\save\DocPortal_institution_Workflow
xcopy $INSTALL_PATH\workflow\institution\DocPortal? $INSTALL_PATH\save\DocProtal_institution_Workflow/ /s /h /i

md $INSTALL_PATH\save\DocPortal_author_Workflow
xcopy $INSTALL_PATH\workflow\author\DocPortal? $INSTALL_PATH\save\DocPortal_author_Workflow/ /s /h /i

md $INSTALL_PATH\save\DocPortal_document_Workflow
xcopy $INSTALL_PATH\workflow\document\DocPortal? $INSTALL_PATH\save\DocPortal_document_Workflow/ /s /h /i

rem Save the user

call $INSTALL_PATH\bin\SaveUser.cmd %PW%

rem Save the classification

md $INSTALL_PATH\save\classification
xcopy $INSTALL_PATH\content\classification $INSTALL_PATH\save\classification\ /s /h /i 

rem Print content

cd $INSTALL_PATH\save\tree /f /a > inhalt.txt
call $INSTALL_PATH\bin\SaveList.cmd

echo Done.