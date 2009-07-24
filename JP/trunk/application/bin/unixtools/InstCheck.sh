#!/bin/bash
#
# Check the XML Schema with a parser sample
#
# InstCheck [ <file.xml> ]

CONTENT=data/workflow
THIS_HOME=$DOCPORTAL_HOME

DIR=`echo "$THIS_HOME $CONTENT" | awk '{printf("%s/%s/institution",$1,$2)}'`
if [ $# = 1 ]
then
  f1=`echo "$DIR $1" | awk '{printf("%s/%s",$1,$2)}'`
  if [ -f $f1 ]
  then
    cd $DIR
    $THIS_HOME/build/bin/mycore.sh check file $1
  else
    if [ -f $1 ]
    then
      $THIS_HOME/build/bin/mycore.sh check file $1
    else
      echo "File $1 not found."
      echo "usage: InstCheck.sh [<file.xml>]"
      echo " "
    fi
  fi
else
  for F in `ls $DIR/*.xml`
  do
    pwd
    $THIS_HOME/build/bin/mycore.sh check file $F
  done
fi
