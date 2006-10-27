#!/bin/bash 
#
# Count all dataset of one Project in the XML SQL tables
#
# SQLListDB2.sh
#

cd $DOCPORTAL_HOME/bin

db2 connect to icmnlsdb >& /dev/null
A=`db2 "select COUNT(MCRID) from $1 where ( MCRID like '%$2%')"` 
db2 quit >& /dev/null

B=`echo $A | awk '{printf($3)}'`

echo "$B"
