#!/bin/bash
#
# Save the privilegs / groups /users
#
# MigrateUser.sh
#

TMP=SaveUser.tmp
DIR=$DOCPORTAL_HOME/migrate/user

rm -f $TMP
mkdir -p $DIR

echo "change to user administrator with alleswirdgut" > $TMP
echo "save all privileges to file $DIR/privileges.xml" >> $TMP
echo "save all groups to file $DIR/groups.xml" >> $TMP
echo "save all users to file $DIR/users.xml" >> $TMP
echo "quit" >> $TMP

cat < $TMP | $DOCPORTAL_HOME/bin/mycore.sh

rm -f $TMP

