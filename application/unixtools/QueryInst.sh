#!/bin/bash 
#
# query for document
#
# DocQuery.sh 
#

THIS_HOME=$DOCPORTAL_HOME

  comm="run local query"
# comm="run distributed query"

# check the empty variant
# result direct from SQL
    q="objectType = institution"


$THIS_HOME/bin/mycore.sh $comm $q 
