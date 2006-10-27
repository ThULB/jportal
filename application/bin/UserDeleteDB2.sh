#!/bin/bash
#
# delete the user system
#
# UserDeleteDB.sh
#

cd $THIS_HOME
THIS_HOME=`pwd`/..

rm -f tmp
cat > tmp <<\EOF
connect to icmnlsdb
drop table DOCUSERS
drop table DOCGROUPS
drop table DOCGROUPMEMBERS
drop table DOCGROUPADMINS
drop table DOCPRIVS
drop table DOCPRIVSLOOKUP
quit
EOF

db2 < tmp

rm -f tmp

