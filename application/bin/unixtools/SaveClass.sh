#!/bin/bash
#
# Save the classifications
#
# SaveClass.sh
#

TMP=SaveClass.tmp
DIR=$DOCPORTAL_HOME/save/class

rm -f $TMP
mkdir -p $DIR

echo "export all classifications to $DIR with save" >> $TMP
echo "quit" >> $TMP

cat < $TMP | $DOCPORTAL_HOME/build/bin/mycore.sh

rm -f $TMP

