#!/bin/bash
#
# load a author for the sample
#
# SAuthLoad.sh [<path> | <file.xml>]
#

CONTENT=workflow
THIS_HOME=$DOCPORTAL_HOME

DIR=`echo "$THIS_HOME $CONTENT" | awk '{printf("%s/%s/author",$1,$2)}'`
if [ $# = 1 ]
then
  f1=`echo "$DIR $1" | awk '{printf("%s/%s",$1,$2)}'`
  if [ -f $f1 ]
  then
    comm="load object from file"
    $THIS_HOME/bin/mycore.sh $comm $f1
  else
    if [ -d $1 ]
    then
      comm="load all objects from directory"
      $THIS_HOME/bin/mycore.sh $comm $1
    else
      echo "File or directory $1 not found."
      echo "usage: SAuthLoad.sh [<path> | <file.xml>]"
      echo " "
    fi
  fi
else
  if [ -d $DIR ]
  then
    comm="load all objects from directory"
    $THIS_HOME/bin/mycore.sh $comm $DIR
  else
    echo "Directory $DIR not found."
    echo "usage: SAuthLoad.sh [<path> | <file.xml>]"
    echo " "
  fi
fi

