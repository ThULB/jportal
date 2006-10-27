#!/bin/bash
#
# delete one or more author
#
# AuthDelete <number> [<number>]
#

THIS_HOME=$DOCPORTAL_HOME

prefix=DocPortal

if [ $# = 1 ]
then
  comm=`echo "$prefix $1" | awk '{printf("delete object %s_author_%s",$1,$2)}'`
  $THIS_HOME/bin/mycore.sh $comm
  exit
fi

if [ $# = 2 ]
then
  f1=`echo "$prefix $1" | awk '{printf("%s_author_%s",$1,$2)}'`
  f2=`echo "$prefix $2" | awk '{printf("%s_author_%s",$1,$2)}'`
  comm="delete object from $f1 to $f2"
  $THIS_HOME/bin/mycore.sh $comm
  exit
fi

echo "usage: AuthDelete <number> [<number>]"
echo " "

