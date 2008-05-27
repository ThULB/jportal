#!/bin/bash 

mkdir -p $DOCPORTAL_HOME/testResults
$DOCPORTAL_HOME/build/bin/mycore.sh export classification DocPortal_class_00000009 to $DOCPORTAL_HOME/testResults/DocPortal_class_00000009.xml with save
$DOCPORTAL_HOME/build/bin/mycore.sh delete classification DocPortal_class_00000006
$DOCPORTAL_HOME/build/bin/mycore.sh delete classification DocPortal_class_00000009
$DOCPORTAL_HOME/build/bin/mycore.sh load classification from file $DOCPORTAL_HOME/modules/module-docportal/classifications/DocPortal_class_00000009.xml
$DOCPORTAL_HOME/build/bin/mycore.sh update classification from file $DOCPORTAL_HOME/modules/module-docportal/classifications/DocPortal_class_00000009.xml
 