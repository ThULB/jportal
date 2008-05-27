#!/bin/bash 
#
# query for document
#
# AuthorQuery.sh 
#

THIS_HOME=$DOCPORTAL_HOME

  comm="run local query"
# comm="run distributed query"

# check the empty variant
# result direct from SQL
    q="objectType = author"


$THIS_HOME/build/bin/mycore.sh $comm $q 
