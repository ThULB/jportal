
# The crontab version of Save.sh

D=`date '+%Y%m%d'`
$DOCPORTAL_HOME/bin/Save.sh > $DOCPORTAL_HOME/logs/save$D.txt

