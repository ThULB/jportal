#!/bin/bash
#
# Migrate the data of a content
#
# MigrateContent.sh table prefix type
# MigrateContent.sh MCRXMLTABLE DocPortal author
#

tab=$1
prefix=$2
type=$3
base=`echo "$prefix $type"| awk '{printf("%s_%s",$1,$2)}'`
mypath=`echo "$DOCPORTAL_HOME $prefix $type" | awk '{printf("%s/migrate/%s_%s_Server",$1,$2,$3)}'`

TMP1=MigrateContent_1.tmp
TMP2=MigrateContent_2.tmp
A=MigrateContent_A.tmp
B=MigrateContent_B.tmp
C=MigrateContent_C.tmp
mkdir -p $mypath
cd $mypath

rm -f $TMP1
rm -f $TMP2
rm -f $A
rm -f $B
rm -f $C

# for hsqldb
if [ $SQLTYPE = 'hsqldb' ]
then
echo "select MCRID from $tab where ( MCRID like '$base%');"  > $TMP1
export CLASSPATH=$CLASSPATH:$MYCORE_HOME/lib/hsqldb_1_7_3.jar
$JAVA_HOME/bin/java -Xmx256m org.hsqldb.util.SqlTool --rcfile $DOCPORTAL_HOME/bin/sqltool.rc localhost-sa < $TMP1 > $TMP2
fgrep $base $TMP2 | sed 's/sql>//' > $A
fi

# for mysql
if [ $SQLTYPE = 'mysql' ]
then
echo "use mycore;" > $TMP1
echo "select MCRID from $tab where ( MCRID like '$base%');"  >> $TMP1
echo "quit" >> $TMP1 
mysql < $TMP > $A
fi

# for DB2
if [ $SQLTYPE = 'db2' ]
then
db2 connect to icmnlsdb >& /dev/null
db2 "select MCRID from $tab where ( MCRID like '$base%')"  > $A
db2 quit >& /dev/null
fi

cat $A | grep $base > $B

for D in `cat $B`
  do
  echo "export object $D to directory $mypath with migration13" >> $C
  done
echo "quit" >> $C

cat < $C | $DOCPORTAL_HOME/bin/mycore.sh

rm -f $TMP1
rm -f $TMP2
rm -f $A
rm -f $B
rm -f $C

