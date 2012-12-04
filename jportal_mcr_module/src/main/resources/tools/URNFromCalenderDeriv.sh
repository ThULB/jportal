#!/bin/bash
txt=false
txtP=""
xml=false
xmlP=""
con=true
optC=0

help()
{
cat << EOF
usage: $0 [OPTION] [path to derivate]

This script search for derivate.

example: $0 -n -x ./derviate.xml -t ./derviate.txt ./docportal/data/metadata/jportal/derivate/

OPTIONS:
-h 		Show help message
-n 		No Output in console
-t [path]	Output in txt file
-x [path]	Output in xml file
EOF
}

while getopts "ht:x:n" opt; do
  case $opt in
   "h")
     help
     exit 1
    ;;
   "t")
    txt=true
    txtP=$OPTARG
    optC=$(($optC+2))
    echo "" > $txtP
    ;;
   "x")
    xml=true
    xmlP=$OPTARG
    optC=$(($optC+2))
    echo "<calendarderivate>" > $xmlP
    ;;
   "n")
    con=false
    optC=$(($optC+1))
    ;;
   \?)
    echo "Invalid option: -$OPTARG"
    echo "Use $0 -h for help."
    exit 1
    ;;
   :)
    echo "Option -$OPTARG requires an argument."
    echo "Use $0 -h for help."
    exit 1
    ;;
   esac
done

if [ $# = $(($optC+1)) ]; then
 path=${@:$#}
else
 path=""
fi

if [ "$path" != "" ]; then

 for i in `grep -r -l "file name=\"/K_*_*" $path`;do

  if [ $con = true ]; then
   grep -oP '(?<= ID=").*|(?<=xlink:href=").*|(?<=fileset urn=").*' $i | cut -d""\" -f1
   echo ""
  fi

  if [ $txt = true ]; then
   grep -oP '(?<= ID=").*|(?<=xlink:href=").*|(?<=fileset urn=").*' $i | cut -d""\" -f1 >> $txtP
   echo "" >> $txtP
  fi
 
  if [ $xml = true ]; then
   echo "<derivate>" >> $xmlP
   echo "<id>" >> $xmlP
   grep -oP '(?<= ID=").*' $i | cut -d""\" -f1 >> $xmlP
   echo "</id>" >> $xmlP
   echo "<parent>" >> $xmlP
   grep -oP '(?<=xlink:href=").*' $i | cut -d""\" -f1 >> $xmlP
   echo "</parent>" >> $xmlP
   echo "<urn>" >> $xmlP
   grep -oP '(?<=fileset urn=").*' $i | cut -d""\" -f1 >> $xmlP
   echo "</urn>" >> $xmlP
   echo "</derivate>" >> $xmlP
  fi
 done
  if [ $xml = true ]; then
   echo "</calendarderivate>" >> $xmlP
  fi
else
  echo "no Path"
  echo "Use $0 -h for help."
fi

