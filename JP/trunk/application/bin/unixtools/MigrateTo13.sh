#!/bin/bash
#
# Migrate all data to the new (1.3) access system
#

SQL=`cat $DOCPORTAL_HOME/config/mycore.properties.private | grep -v '#' | grep 'MCR.persistence_sql_type'`
SQLTYPE=`echo "$SQL"| cut -f2 -d= `
export SQLTYPE

#rm -Rf $DOCPORTAL_HOME/migrate/*
mkdir -p $DOCPORTAL_HOME/migrate
cd $DOCPORTAL_HOME/migrate

$DOCPORTAL_HOME/bin/MigrateUser.sh

$DOCPORTAL_HOME/bin/MigrateContent.sh MCRXMLTABLE DocPortal institution
$DOCPORTAL_HOME/bin/MigrateContent.sh MCRXMLTABLE DocPortal author
$DOCPORTAL_HOME/bin/MigrateContent.sh MCRXMLTABLE DocPortal document
$DOCPORTAL_HOME/bin/MigrateDerivate.sh MCRXMLTABLE DocPortal derivate

$DOCPORTAL_HOME/bin/MigrateList.sh

