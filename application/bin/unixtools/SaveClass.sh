#!/bin/bash
#
# Save the classifications
#
# SaveClass.sh
#

TMP=SaveClass.tmp

BASE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep -v '%MCR.basedir%' | grep 'MCR.basedir'`
BASEDIR=`echo "$BASE"| cut -f2 -d= `
export BASEDIR

SAVE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Save.FileSystem'`
SAVEDIR=`echo "$SAVE"| cut -f2 -d= | sed -e "s#%MCR.basedir%#$BASEDIR#g"`
export SAVEDIR

DIR=$SAVEDIR/class

rm -f $TMP
mkdir -p $DIR

echo "export all classifications to $DIR with save" >> $TMP
echo "quit" >> $TMP

cat < $TMP | $INSTALL_HOME/build/bin/mycore.sh

rm -f $TMP
