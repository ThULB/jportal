#!/bin/bash 

mkdir -p $DOCPORTAL_HOME/testResults
$DOCPORTAL_HOME/build/bin/mycore.sh get last ID for base DocPortal_document
$DOCPORTAL_HOME/build/bin/mycore.sh get next ID for base DocPortal_document
$DOCPORTAL_HOME/build/bin/mycore.sh repair metadata search of type document
$DOCPORTAL_HOME/build/bin/mycore.sh repair metadata search of ID DocPortal_document_07910403
$DOCPORTAL_HOME/build/bin/mycore.sh repair derivate search of ID DocPortal_derivate_00410903
$DOCPORTAL_HOME/build/bin/mycore.sh delete derivate DocPortal_derivate_00410902
$DOCPORTAL_HOME/build/bin/mycore.sh delete object DocPortal_document_00410902
$DOCPORTAL_HOME/build/bin/mycore.sh delete object DocPortal_document_00410901