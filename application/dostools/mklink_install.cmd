%systemdrive%
cd "%userprofile%\Startmen*\programme"
mkdir "DocPortal"
cd DocPortal
$INSTALL_PATH\dostools\shortcut\shortcut /f:"Jettystart.lnk" /a:C /w:"$INSTALL_PATH" /t:"$INSTALL_PATH\bin\jettystart.cmd" /r:7 /i:$INSTALL_PATH\webpages\images\mycore-logo-start.ico /D:"Start Jetty-Servers"
$INSTALL_PATH\dostools\shortcut\shortcut /f:"Jettystop.lnk" /a:C /w:"$INSTALL_PATH" /t:"$INSTALL_PATH\bin\jettystop.cmd" /r:7 /i:$INSTALL_PATH\webpages\images\mycore-logo-stop.ico /D:"Stop Jetty Servers"
$INSTALL_PATH\dostools\shortcut\shortcut /f:"HSQLDBstart.lnk" /a:C /w:"$INSTALL_PATH" /t:"$INSTALL_PATH\bin\hsqldbstart.cmd" /r:7 /i:$INSTALL_PATH\webpages\images\mycore-logo-start.ico /D:"Start HSQLDB-Servers"
$INSTALL_PATH\dostools\shortcut\shortcut /f:"HSQLDBstop.lnk" /a:C /w:"$INSTALL_PATH" /t:"$INSTALL_PATH\bin\hsqldbstop.cmd" /r:7 /i:$INSTALL_PATH\webpages\images\mycore-logo-stop.ico /D:"Stop HSQLDB-Servers"
$INSTALL_PATH\dostools\shortcut\shortcut /f:"UnInstall.lnk" /a:C /t:"$INSTALL_PATH\Uninstaller\uninstaller.jar" /r:7 /i:$INSTALL_PATH\webpages\images\SHELL32_262.ico /D:"Uninstall"
