#!/bin/bash
#
# Save the data of derivates
#
# SaveDerivate.sh table prefix type
# SaveDerivate.sh MCRXMLTABLE DocPortal derivate
#

tab=$1
prefix=$2
type=$3
base=`echo "$prefix $type"| awk '{printf("%s_%s",$1,$2)}'`
mypath=`echo "$DOCPORTAL_HOME $prefix $type" | awk '{printf("%s/save/%s_%s_Server",$1,$2,$3)}'`

SQL=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Persistence.SQL.Database.Name'`
SQLDATABASE=`echo "$SQL"| cut -f2 -d= `
export SQLDATABASE

TMP1=SaveContent_1.tmp
TMP2=SaveContent_2.tmp
A=SaveContent_A.tmp
B=SaveContent_B.tmp
C=SaveContent_C.tmp
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
echo "select MCRID from $tab where ( MCRID like '$base%');"  >> $TMP1
export CLASSPATH=$CLASSPATH:$DOCPORTAL_HOME/build/lib/third-party.jar
$JAVA_HOME/bin/java -Xmx256m org.hsqldb.util.SqlTool --rcfile $DOCPORTAL_HOME/build/bin/sqltool.rc localhost-sa < $TMP1 > $TMP2
fgrep $base $TMP2 | sed 's/sql>//' > $A
fi

# for mysql
if [ $SQLTYPE = 'mysql' ]
then
echo "use $SQLDATABASE;" > $TMP1
echo "select MCRID from $tab where ( MCRID like '$base%');"  >> $TMP1
echo "quit" >> $TMP1
mysql < $TMP > $A
fi

# for DB2
if [ $SQLTYPE = 'db2' ]
then
db2 connect to $SQLDATABASE >& /dev/null
db2 "select MCRID from $tab where ( MCRID like '$base%')"  > $A
db2 quit >& /dev/null
fi

cat $A | grep $base > $B

for D in `cat $B`
  do
  echo "export derivate $D to directory $mypath with save" >> $C
  done
echo "quit" >> $C

E=SaveContent_E.tmp
split -l 100 $C $E

for D in `ls $E*`
  do
  echo "quit" >> $D
  cat < $D | $DOCPORTAL_HOME/build/bin/mycore.sh
  done

rm -f $TMP1
rm -f $TMP2
rm -f $A
rm -f $B
rm -f $C
rm -f $E*

