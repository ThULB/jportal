#!/bin/bash 
#
# Count all dataset of one Project in the XML SQL tables
#
# SQLCountDB2.sh
#

cd $DOCPORTAL_HOME/build/bin

SQL=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Persistence.SQL.Database.Name'`
SQLDATABASE=`echo "$SQL"| cut -f2 -d= `
export SQLDATABASE

db2 connect to $SQLDATABASE >& /dev/null
A=`db2 "select COUNT(MCRID) from $1 where ( MCRID like '%$2%')"` 
db2 quit >& /dev/null

B=`echo $A | awk '{printf($3)}'`

echo "$B"
