#!/bin/bash
#
# delete the classification system
#
# ClassDeleteDB2.sh
#

THIS_HOME=`pwd`/..
cd $THIS_HOME

rm -f tmp
cat > tmp <<\EOF
connect to icmnlsdb
drop table DOCCLASS
drop table DOCCLASSLABEL
drop table DOCCATEG
drop table DOCCATEGLABEL
quit
EOF

db2 < tmp

rm -f tmp

