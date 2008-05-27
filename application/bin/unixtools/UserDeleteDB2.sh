#!/bin/bash
#
# delete the user system
#
# UserDeleteDB.sh
#

cd $THIS_HOME
THIS_HOME=$DOCPORTAL_HOME

rm -f tmp
cat > tmp <<\EOF
connect to icmnlsdb
drop table DOCUSERS
drop table DOCGROUPS
drop table DOCGROUPMEMBERS
drop table DOCGROUPADMINS
quit
EOF

db2 < tmp

rm -f tmp

