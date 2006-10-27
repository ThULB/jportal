#!/bin/bash
#
# update a classification
#
# ClassUpdate.sh [<path> | <file.xml>]
#

THIS_HOME=$DOCPORTAL_HOME

if [ $# = 1 ]
then
  f1=`echo "$THIS_HOME $1" | awk '{printf("%s/content/classification/%s",$1,$2)}'`
  if [ -f $f1 ]
  then
    comm="update classification from file"
    $THIS_HOME/bin/mycore.sh $comm $f1
    exit
  fi
  if [ -d $1 ]
  then
    comm="update all classifications from directory"
    $THIS_HOME/bin/mycore.sh $comm $1
    exit
  else
    echo "File or directory $1 not found."
    echo "usage: ClassUpdate.sh [<path> | <file.xml>]"
    echo " "
  fi
else
  if [ -d $THIS_HOME/content/classification ]
  then
    comm="update all classifications from directory"
    $THIS_HOME/bin/mycore.sh $comm $THIS_HOME/content/classification
    exit
  fi
  echo "Directory $THIS_HOME/content/classification not found."
  echo "usage: ClassUpdate.sh [<path> | <file.xml>]"
  echo " "
fi

