#!/bin/bash
#
# Count all dataset of one Project in the XML SQL tables
#
# SQLListMySQL.sh
#

cd $DOCPORTAL_HOME/build/bin

TMP=SQLListMySQL.tmp
rm -f $TMP

SQL=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Persistence.SQL.Database.Name'`
SQLDATABASE=`echo "$SQL"| cut -f2 -d= `
export SQLDATABASE

echo "use $SQLDATABASE;" > $TMP
echo "select COUNT(MCRID) from $1 where ( MCRID like '%$2%');" >> $TMP
echo "quit" >> $TMP

A=`mysql < $TMP` 

B=`echo $A | awk '{printf($2)}'`

rm -f $TMP

echo "$B"
