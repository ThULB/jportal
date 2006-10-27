#!/bin/bash
#
# Check the XML Schema with a parser sample
#
# SAuthCheck [ <file.xml> ]

CONTENT=workflow
THIS_HOME=$DOCPORTAL_HOME

DIR=`echo "$THIS_HOME $CONTENT" | awk '{printf("%s/%s/author",$1,$2)}'`
if [ $# = 1 ]
then
  f1=`echo "$DIR $1" | awk '{printf("%s/%s",$1,$2)}'`
  if [ -f $f1 ]
  then
    cd $DIR
    $THIS_HOME/bin/mycore.sh check file $1
  else
    if [ -f $1 ]
    then
      $THIS_HOME/bin/mycore.sh check file $1
    else
      echo "File $1 not found."
      echo "usage: SAuthCheck.sh [<file.xml>]"
      echo " "
    fi
  fi
else
  for F in `ls $DIR/*.xml`
  do
    $THIS_HOME/bin/mycore.sh check file $F
  done
fi
