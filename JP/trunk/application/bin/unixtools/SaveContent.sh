#!/bin/bash
#
# Save the data of a content
#
# SaveContent.sh table prefix type
# SaveContent.sh MCRXMLTABLE DocPortal author
#

tab=$1
prefix=$2
type=$3

BASE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep -v '%MCR.basedir%' | grep 'MCR.basedir'`
BASEDIR=`echo "$BASE"| cut -f2 -d= `
export BASEDIR

SAVE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Save.FileSystem'`
SAVEDIR=`echo "$SAVE"| cut -f2 -d= | sed -e "s#%MCR.basedir%#$BASEDIR#g"`
export SAVEDIR

base=`echo "$prefix $type"| awk '{printf("%s_%s",$1,$2)}'`
mypath=`echo "$SAVEDIR $prefix $type" | awk '{printf("%s/%s_%s_Server",$1,$2,$3)}'`

SQL=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Persistence.SQL.Database.Name'`
SQLNAME=`echo "$SQL"| cut -f2 -d= `
export SQLNAME

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
echo "select MCRID from $tab where ( MCRID like '$base%');"  > $TMP1
export CLASSPATH=$CLASSPATH:$DOCPORTAL_HOME/build/lib/hsqldb_1_8_0_7.jar
$JAVA_HOME/bin/java -Xmx256m org.hsqldb.util.SqlTool --rcfile $DOCPORTAL_HOME/build/bin/sqltool.rc localhost-sa < $TMP1 > $TMP2
fgrep $base $TMP2 | sed 's/sql>//' > $A
fi

# for mysql
if [ $SQLTYPE = 'mysql' ]
then
echo "use $SQLNAME;" > $TMP1
echo "select MCRID from $tab where ( MCRID like '$base%');"  >> $TMP1
echo "quit" >> $TMP1
mysql < $TMP1 > $A
fi

# for DB2
if [ $SQLTYPE = 'db2' ]
then
db2 connect to $SQLNAME >& /dev/null
db2 "select MCRID from $tab where ( MCRID like '$base%')"  > $A
db2 quit >& /dev/null
fi

cat $A | grep $base > $B

for D in `cat $B`
  do
  if [ $# != 4 ]; then
    echo "export object $D to directory $mypath with save" >> $C
  else
    echo "export object $D to directory $mypath with $4" >> $C
  fi
  done
echo "quit" >> $C

cat < $C | $DOCPORTAL_HOME/build/bin/mycore.sh

rm -f $TMP1
rm -f $TMP2
rm -f $A
rm -f $B
rm -f $C
