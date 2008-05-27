#!/bin/bash
#
# Save the permissions / groups /users
#
# SaveUser.sh
#

TMP=SaveUser.tmp
DIR=$DOCPORTAL_HOME/save/user

rm -f $TMP
mkdir -p $DIR

echo "change to user administrator with alleswirdgut" > $TMP
echo "export all groups to file $DIR/groups.xml" >> $TMP
echo "export all users to file $DIR/users.xml" >> $TMP
echo "export all permissions to file $DIR/permissions.xml" >> $TMP
echo "quit" >> $TMP

cat < $TMP | $DOCPORTAL_HOME/build/bin/mycore.sh

rm -f $TMP

