#!/bin/bash
#
# Repair all search collections and extended tables with data from the 
# XML SQL store
#

$DOCPORTAL_HOME/build/bin/mycore.sh repair metadata search of type institution
$DOCPORTAL_HOME/build/bin/mycore.sh repair metadata search of type author
$DOCPORTAL_HOME/build/bin/mycore.sh repair metadata search of type document

#$DOCPORTAL_HOME/build/bin/mycore.sh repair derivate search of type derivate

