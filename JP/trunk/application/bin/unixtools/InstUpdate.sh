#!/bin/bash
#
# update a institution for the sample
#
# InstUpdate.sh [<path> | <file.xml>]
#

CONTENT=data/workflow
THIS_HOME=$DOCPORTAL_HOME

DIR=`echo "$THIS_HOME $CONTENT" | awk '{printf("%s/%s/institution",$1,$2)}'`
if [ $# = 1 ]
then
  f1=`echo "$DIR $1" | awk '{printf("%s/%s",$1,$2)}'`
  if [ -f $f1 ]
  then
    comm="update object from file"
    $THIS_HOME/build/bin/mycore.sh $comm $f1
  else
    if [ -d $1 ]
    then
      comm="update all objects from directory"
      $THIS_HOME/build/bin/mycore.sh $comm $1
    else
      echo "File or directory $1 not found."
      echo "usage: InstUpdate.sh [<path> | <file.xml>]"
      echo " "
    fi
  fi
else
  if [ -d $DIR ]
  then
    comm="update all objects from directory"
    $THIS_HOME/build/bin/mycore.sh $comm $DIR
  else
    echo "Directory $DIR not found."
    echo "usage: InstUpdate.sh [<path> | <file.xml>]"
    echo " "
  fi
fi

