#!/bin/bash 
#
# Check the XML Schema with a parser sample
#
# ClassCheck.sh [ <file.xml> ]

THIS_HOME=$DOCPORTAL_HOME

if [ $# = 1 ]
then
  f1=`echo "$THIS_HOME $1" | awk '{printf("%s/modules/docportal/classifications/%s",$1,$2)}'`
  if [ -f $f1 ]
  then
    cd $THIS_HOME/modules/docportal/classifications
    $THIS_HOME/build/bin/mycore.sh check file $1
    exit
  fi
  echo "File $1 not found."
  echo "usage: ClassCheck.sh [<file.xml>]"
  echo " "
else
  cd $THIS_HOME/modules/docportal/classifications
  for F in `ls *.xml`
  do
    $THIS_HOME/build/bin/mycore.sh check file $F
  done
fi

