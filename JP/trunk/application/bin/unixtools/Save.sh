#!/bin/bash 
#
# save all documents from the server an from the workflow
#

SQL=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Persistence.SQL.Type'`
SQLTYPE=`echo "$SQL"| cut -f2 -d= `
export SQLTYPE

BASE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep -v '%MCR.basedir%' | grep 'MCR.basedir'`
BASEDIR=`echo "$BASE"| cut -f2 -d= `
export BASEDIR

SAVE=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Save.FileSystem'`
SAVEDIR=`echo "$SAVE"| cut -f2 -d=  | sed -e "s#%MCR.basedir%#$BASEDIR#g"`
export SAVEDIR

DATA=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep -v '%MCR.datadir%' | grep 'MCR.datadir'`
DATADIR=`echo "$DATA"| cut -f2 -d=  | sed -e "s#%MCR.basedir%#$BASEDIR#g"`
export DATADIR

#rm -Rf $SAVEDIR/*
mkdir -p $SAVEDIR

# save server data

$DOCPORTAL_HOME/build/bin/SaveContent.sh MCRXMLTABLE DocPortal institution
$DOCPORTAL_HOME/build/bin/SaveContent.sh MCRXMLTABLE DocPortal author
$DOCPORTAL_HOME/build/bin/SaveContent.sh MCRXMLTABLE DocPortal document
$DOCPORTAL_HOME/build/bin/SaveDerivate.sh MCRXMLTABLE DocPortal derivate

# save workflow

mkdir -p $SAVEDIR/DocPortal_institution_Workflow
cp -R $DATADIR/workflow/institution/* $SAVEDIR/DocPortal_institution_Workflow

mkdir -p $SAVEDIR/DocPortal_author_Workflow
cp -R $DATADIR/workflow/author/* $SAVEDIR/DocPortal_author_Workflow

mkdir -p $SAVEDIR/DocPortal_document_Workflow
cp -R $DATADIR/workflow/document/* $SAVEDIR/DocPortal_document_Workflow

# Save the user

$DOCPORTAL_HOME/build/bin/SaveUser.sh

# Save the classifications

$DOCPORTAL_HOME/build/bin/SaveClass.sh

# write list

cd $SAVEDIR
ls -Rl > inhalt.txt

$DOCPORTAL_HOME/build/bin/SaveList.sh

