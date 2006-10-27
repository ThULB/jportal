#!/bin/bash
#
# Check the XML Schema with a parser sample
#
# ClassCheck.sh [ <file.xml> ]

THIS_HOME=$DOCPORTAL_HOME

if [ $# = 1 ]
then
  f1=`echo "$THIS_HOME $1" | awk '{printf("%s/content/classification/%s",$1,$2)}'`
  if [ -f $f1 ]
  then
    cd $THIS_HOME/content/classification
    $THIS_HOME/bin/mycore.sh check file $1
    exit
  fi
  f2=`echo "$DOCPORTAL_HOME $1" | awk '{printf("%s/content/classification/%s",$1,$2)}'`
  if [ -f $f2 ]
  then
    cd $DOCPORTAL_HOME/content/classification
    $THIS_HOME/bin/mycore.sh check file $1
    exit
  fi
  echo "File $1 not found."
  echo "usage: ClassCheck.sh [<file.xml>]"
  echo " "
else
  cd $DOCPORTAL_HOME/content/classification
  for F in `ls *.xml`
  do
    $THIS_HOME/bin/mycore.sh check file $F
  done
  cd $THIS_HOME/content/classification
  for F in `ls *.xml`
  do
    $THIS_HOME/bin/mycore.sh check file $F
  done
fi

