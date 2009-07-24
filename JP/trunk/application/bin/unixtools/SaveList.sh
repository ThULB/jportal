#!/bin/bash
#
# list all saveed documents from the server an from the workflow
#

BASE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep -v '%MCR.basedir%' | grep 'MCR.basedir'`
BASEDIR=`echo "$BASE"| cut -f2 -d= `
export BASEDIR

SAVE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Save.FileSystem'`
SAVEDIR=`echo "$SAVE"| cut -f2 -d= | sed -e "s#%MCR.basedir%#$BASEDIR#g"`
export SAVEDIR

cd $SAVEDIR
L=SaveList.txt
rm -Rf $L

echo " " >> $L
echo "Uebersicht der gesicherten Daten" >> $L
echo "================================" >> $L
echo " " >> $L

cd $SAVEDIR
A=`ls DocPortal_institution_Server|grep xml|wc|awk '{printf($1)}'`
if [ $SQLTYPE = 'db2' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountDB2.sh MCRXMLINST _institution_`
fi
if [ $SQLTYPE = 'mysql' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountMySQL.sh MCRXMLINST _institution_`
fi
if [ $SQLTYPE = 'hsqldb' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountHSQLDB.sh MCRXMLTABLE DocPortal_institution_`
fi
echo "  DocPortal_institution_Server: in save $A   in DB $B" >> $L

cd $SAVEDIR
A=`ls DocPortal_author_Server|grep xml|wc|awk '{printf($1)}'`
if [ $SQLTYPE = 'db2' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountDB2.sh MCRXMLAUTH _author_`
fi
if [ $SQLTYPE = 'mysql' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountMySQL.sh MCRXMLAUTH _author_`
fi
if [ $SQLTYPE = 'hsqldb' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountHSQLDB.sh MCRXMLTABLE DocPortal_author_`
fi
echo "  DocPortal_author_Server     : in save $A   in DB $B" >> $L

cd $SAVEDIR
A=`ls DocPortal_document_Server|grep xml|wc|awk '{printf($1)}'`
if [ $SQLTYPE = 'db2' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountDB2.sh MCRXMLDOC _document_`
fi
if [ $SQLTYPE = 'mysql' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountMySQL.sh MCRXMLDOC _document_`
fi
if [ $SQLTYPE = 'hsqldb' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountHSQLDB.sh MCRXMLTABLE DocPortal_document_`
fi
echo "  DocPortal_document_Server   : in save $A   in DB $B" >> $L

cd $SAVEDIR
A=`ls DocPortal_derivate_Server|grep xml|wc|awk '{printf($1)}'`
if [ $SQLTYPE = 'db2' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountDB2.sh MCRXMLDER _derivate_`
fi
if [ $SQLTYPE = 'mysql' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountMySQL.sh MCRXMLDER _derivate_`
fi
if [ $SQLTYPE = 'hsqldb' ]
then
B=`$DOCPORTAL_HOME/build/bin/SQLCountHSQLDB.sh MCRXMLTABLE DocPortal_derivate_`
fi
echo "  DocPortal_derivate_Server   : in save $A   in DB $B" >> $L

cd $SAVEDIR
echo " " >> $L
A=`ls DocPortal_institution_Workflow|grep xml|grep institution|wc|awk '{printf($1)}'`
echo "  DocPortal_institution_Workflow  : in save $A " >> $L

A=`ls DocPortal_author_Workflow|grep xml|grep author|wc|awk '{printf($1)}'`
echo "  DocPortal_author_Workflow       : in save $A " >> $L

A=`ls DocPortal_document_Workflow|grep xml|grep document|wc|awk '{printf($1)}'`
echo "  DocPortal_document_Workflow     : in save $A " >> $L

A=`ls DocPortal_document_Workflow|grep xml|grep derivate|wc|awk '{printf($1)}'`
echo "  DocPortal_document_Workflow (D) : in save $A " >> $L

echo " " >> $L

cat $L

