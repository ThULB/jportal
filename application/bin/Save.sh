#!/bin/bash 
#
# save all documents from the server an from the workflow
#

SQL=`cat $DOCPORTAL_HOME/config/mycore.properties.private | grep -v '#' | grep 'MCR.persistence_sql_type'`
SQLTYPE=`echo "$SQL"| cut -f2 -d= `
export SQLTYPE

#rm -Rf $DOCPORTAL_HOME/save/*
mkdir -p $DOCPORTAL_HOME/save

# save server data

$DOCPORTAL_HOME/bin/SaveContent.sh MCRXMLTABLE DocPortal institution
$DOCPORTAL_HOME/bin/SaveContent.sh MCRXMLTABLE DocPortal author
$DOCPORTAL_HOME/bin/SaveContent.sh MCRXMLTABLE DocPortal document
$DOCPORTAL_HOME/bin/SaveDerivate.sh MCRXMLTABLE DocPortal derivate

# save workflow

mkdir -p $DOCPORTAL_HOME/save/DocPortal_author_Workflow
cp -R $DOCPORTAL_HOME/workflow/author/* $DOCPORTAL_HOME/save/DocPortal_author_Workflow

mkdir -p $DOCPORTAL_HOME/save/DocPortal_document_Workflow
cp -R $DOCPORTAL_HOME/workflow/document/* $DOCPORTAL_HOME/save/DocPortal_document_Workflow

mkdir -p $DOCPORTAL_HOME/save/DocPortal_institution_Workflow
cp -R $DOCPORTAL_HOME/workflow/institution/* $DOCPORTAL_HOME/save/DocPortal_institution_Workflow

# Save the user

$DOCPORTAL_HOME/bin/SaveUser.sh

# Save the classifications

cp -R $DOCPORTAL_HOME/content/classification $DOCPORTAL_HOME/save/

# write list

cd $DOCPORTAL_HOME/save
ls -Rl > inhalt.txt

$DOCPORTAL_HOME/bin/SaveList.sh

