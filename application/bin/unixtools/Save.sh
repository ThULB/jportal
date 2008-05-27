#!/bin/bash 
#
# save all documents from the server an from the workflow
#

SQL=`cat $DOCPORTAL_HOME/build/config/mycore.properties | grep -v '#' | grep 'MCR.Persistence.SQL.Type'`
SQLTYPE=`echo "$SQL"| cut -f2 -d= `
export SQLTYPE

#rm -Rf $DOCPORTAL_HOME/save/*
mkdir -p $DOCPORTAL_HOME/save

# save server data

$DOCPORTAL_HOME/build/bin/SaveContent.sh MCRXMLTABLE DocPortal institution
$DOCPORTAL_HOME/build/bin/SaveContent.sh MCRXMLTABLE DocPortal author
$DOCPORTAL_HOME/build/bin/SaveContent.sh MCRXMLTABLE DocPortal document
$DOCPORTAL_HOME/build/bin/SaveDerivate.sh MCRXMLTABLE DocPortal derivate

# save workflow

mkdir -p $DOCPORTAL_HOME/save/DocPortal_author_Workflow
cp -R $DOCPORTAL_HOME/data/workflow/author/* $DOCPORTAL_HOME/save/DocPortal_author_Workflow

mkdir -p $DOCPORTAL_HOME/save/DocPortal_document_Workflow
cp -R $DOCPORTAL_HOME/data/workflow/document/* $DOCPORTAL_HOME/save/DocPortal_document_Workflow

mkdir -p $DOCPORTAL_HOME/save/DocPortal_institution_Workflow
cp -R $DOCPORTAL_HOME/data/workflow/institution/* $DOCPORTAL_HOME/save/DocPortal_institution_Workflow

# Save the user

$DOCPORTAL_HOME/build/bin/SaveUser.sh

# Save the classifications

$DOCPORTAL_HOME/build/bin/SaveClass.sh

# write list

cd $DOCPORTAL_HOME/save
ls -Rl > inhalt.txt

$DOCPORTAL_HOME/build/bin/SaveList.sh

