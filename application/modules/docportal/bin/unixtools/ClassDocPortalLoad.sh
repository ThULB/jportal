#!/bin/bash
#
# load a classification
#
# ClassLoad.sh [<path> | <file.xml>]
#

THIS_HOME=$DOCPORTAL_HOME

if [ $# = 1 ]
then
  f1=`echo "$THIS_HOME $1" | awk '{printf("%s/modules/docportal/classifications/%s",$1,$2)}'`
  if [ -f $f1 ]
  then
    comm="load classification from file"
    $THIS_HOME/build/bin/mycore.sh $comm $f1
    exit
  fi
  if [ -d $1 ]
  then
    comm="load all classifications from directory"
    $THIS_HOME/build/bin/mycore.sh $comm $1
    exit
  else
    echo "File or directory $1 not found."
    echo "usage: ClassLoad.sh [<path> | <file.xml>]"
    echo " "
  fi
else
  if [ -d $THIS_HOME/modules/docportal/classifications ]
  then
    comm="load all classifications from directory"
    $THIS_HOME/build/bin/mycore.sh $comm $THIS_HOME/modules/docportal/classifications
    exit
  fi
  echo "Directory $THIS_HOME/modules/docportal/classifications not found."
  echo "usage: ClassLoad.sh [<path> | <file.xml>]"
  echo " "
fi

