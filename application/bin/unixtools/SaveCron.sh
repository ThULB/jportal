
# The crontab version of Save.sh

mkdir $DOCPORTAL_HOME/logs
D=`date '+%Y%m%d'`
$DOCPORTAL_HOME/build/bin/Save.sh > $DOCPORTAL_HOME/logs/save-$D.txt

