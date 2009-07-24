#!/bin/bash 
#
# Count all dataset of one Project in the XML SQL tables
#
# SQLCountHSQLDB.sh TABLE MCRProjectID
#

export CLASSPATH=$CLASSPATH:$DOCPORTAL_HOME/build/lib/hsqldb_1_8_0_7.jar

cd $DOCPORTAL_HOME/build/bin

TMP1=SQLListHSQLDB1.tmp
TMP2=SQLListHSQLDB2.tmp
rm -f $TMP1
rm -f $TMP2

echo "select COUNT(MCRID) from $1 where ( MCRID like '$2%');" > $TMP1

$JAVA_HOME/bin/java -Xmx256m org.hsqldb.util.SqlTool --rcfile $DOCPORTAL_HOME/build/bin/sqltool.rc localhost-sa < $TMP1 > $TMP2
A=`fgrep 'sql>' $TMP2`
B=`echo $A | awk '{printf($2)}'`

rm -f $TMP1
rm -f $TMP2

echo "$B"
