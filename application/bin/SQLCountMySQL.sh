#!/bin/bash
#
# Count all dataset of one Project in the XML SQL tables
#
# SQLListMySQL.sh
#

cd $DOCPORTAL_HOME/bin

TMP=SQLListMySQL.tmp
rm -f $TMP

echo "use mycore;" > $TMP
echo "select COUNT(MCRID) from $1 where ( MCRID like '%$2%');" >> $TMP
echo "quit" >> $TMP

A=`mysql < $TMP` 

B=`echo $A | awk '{printf($2)}'`

rm -f $TMP

echo "$B"
