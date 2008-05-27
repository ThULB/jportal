#!/bin/bash
#
# load a derivate of a document
#
# DerivateLoad.sh [<path> | <file.xml>]
#

CONTENT=data/workflow
THIS_HOME=$DOCPORTAL_HOME

DIR=`echo "$THIS_HOME $CONTENT" | awk '{printf("%s/%s/document",$1,$2)}'`
if [ $# = 1 ]
then
  f1=`echo "$DIR $1" | awk '{printf("%s/%s",$1,$2)}'`
  if [ -f $f1 ]
  then
    comm="load derivate from file"
    $THIS_HOME/build/bin/mycore.sh $comm $f1
  else
    echo "File or directory $1 not found."
    echo "usage: DerivateLoad.sh [<path> | <file.xml>]"
    echo " "
  fi
else
  if [ -d $DIR ]
  then
    comm="load derivate from file"
    TMP=tmp
    rm -f $TMP
    for F in `ls $DIR/*_derivate_*.xml`
      do
      echo "$comm $F" >> $TMP
      done
    echo "quit" >> $TMP
    cat $TMP | $THIS_HOME/build/bin/mycore.sh
    rm -f $TMP
  else
    echo "Directory $THIS_HOME/content/derivates not found."
    echo "usage: DerivateLoad.sh [<path> | <file.xml>]"
    echo " "
  fi
fi


