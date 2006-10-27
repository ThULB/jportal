#!/bin/bash
#
# delete a classification
#
# ClassDelete.sh <number>
#

THIS_HOME=$DOCPORTAL_HOME

if [ $# = 1 ]
then
  comm="delete classification DocPortal_class_$1"
  $THIS_HOME/bin/mycore.sh $comm 
else
  echo "usage: ClassDelete.sh <number>"
  echo " "
fi

