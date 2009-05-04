#!/bin/bash
#
# Save the permissions / groups /users
#
# SaveUser.sh
#

BASE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep -v '%MCR.basedir%' | grep 'MCR.basedir'`
BASEDIR=`echo "$BASE"| cut -f2 -d= `
export BASEDIR

SAVE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Save.FileSystem'`
SAVEDIR=`echo "$SAVE"| cut -f2 -d= | sed -e "s#%MCR.basedir%#$BASEDIR#g"`
export SAVEDIR

TMP=SaveUser.tmp
rm -f $TMP
mkdir -p $SAVEDIR/user

echo "export all groups to file $SAVEDIR/user/groups.xml" >> $TMP
echo "export all users to file $SAVEDIR/user/users.xml" >> $TMP
echo "export all permissions to file $SAVEDIR/user/permissions.xml" >> $TMP
echo "quit" >> $TMP

cat < $TMP | $DOCPORTAL_HOME/build/bin/mycore.sh

rm -f $TMP

