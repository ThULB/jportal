#!/bin/bash
#
# delete the classification system
#
# ClassDeleteMySQL.sh
#

THIS_HOME=`pwd`/..
cd $THIS_HOME

rm -f tmp
cat > tmp <<\EOF
use mycore
drop table DOCCLASS;
drop table DOCCLASSLABEL;
drop table DOCCATEG;
drop table DOCCATEGLABEL;
quit
EOF

mysql < tmp

rm -f tmp

