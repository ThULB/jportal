#!/bin/bash
#
# read data from save of the user system and build single user and group files
#

SOURCE=$DOCPORTAL_HOME/save/user

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
  