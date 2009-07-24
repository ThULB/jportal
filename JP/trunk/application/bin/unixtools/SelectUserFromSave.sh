#!/bin/bash
#
# read data from save of the user system and build single user and group files
#

BASE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep -v '%' | grep 'MCR.basedir'`
BASEDIR=`echo "$BASE"| cut -f2 -d= `
export BASEDIR

SAVE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Save.FileSystem'`
SAVEDIR=`echo "$SAVE"| cut -f2 -d= | sed -e "s#%MCR.basedir%#$BASEDIR#g"`
export SAVEDIR

SOURCE=$SAVEDIR/user

for LIB in `ls $MYCORE_HOME/lib/*.jar`
  do
  export CLASSPATH=$CLASSPATH:$LIB
  done
#echo "$CLASSPATH"

cd $SOURCE

java org.apache.xalan.xslt.Process -IN $SOURCE/users.xml -XSL $DOCPORTAL_HOME/stylesheets/mcruser_split.xsl

for I in `ls user_*.xml`
  do
  cp $I $DOCPORTAL_HOME/config/user/$I
  done

java org.apache.xalan.xslt.Process -IN $SOURCE/groups.xml -XSL $DOCPORTAL_HOME/stylesheets/mcrgroup_split.xsl

for I in `ls group_*.xml`
  do
  cp $I $DOCPORTAL_HOME/config/user/$I
  done
  