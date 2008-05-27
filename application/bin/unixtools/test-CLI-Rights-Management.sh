#!/bin/bash 

mkdir -p $DOCPORTAL_HOME/testResults
$DOCPORTAL_HOME/build/bin/mycore.sh list all permissions
$DOCPORTAL_HOME/build/bin/mycore.sh export all permissions to file $DOCPORTAL_HOME/testResults/permission.xml
