#!/bin/bash
#
# delete the user system
#
# UserDeleteMySQL.sh
#

THIS_HOME=$DOCPORTAL_HOME
cd $THIS_HOME

rm -f tmp
cat > tmp <<\EOF
use mycore
drop table DOCUSERS;
drop table DOCGROUPS;
drop table DOCGROUPMEMBERS;
drop table DOCGROUPADMINS;
quit
EOF

mysql < tmp

rm -f tmp

