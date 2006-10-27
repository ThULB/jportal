#!/bin/bash
#
# delete one or more derivates from the sample
#
# SDerDelete <number> [<number>]
#

THIS_HOME=$DOCPORTAL_HOME

prefix=DocPortal

if [ $# = 1 ]
then
  comm=`echo "$prefix $1" | awk '{printf("delete derivate %s_derivate_%s",$1,$2)}'`
  $THIS_HOME/bin/mycore.sh $comm
  exit
fi

if [ $# = 2 ]
then
  f1=`echo "$prefix $1" | awk '{printf("%s_derivate_%s",$1,$2)}'`
  f2=`echo "$prefix $2" | awk '{printf("%s_derivate_%s",$1,$2)}'`
  comm="delete derivate from $f1 to $f2"
  $THIS_HOME/bin/mycore.sh $comm
  exit
fi

echo "usage: SDerDelete <number> [<number>]"
echo " "

