@echo off

rem
rem Count all dataset of one Project in the XML SQL tables
rem
rem SQLCountHSQLDB.sh
rem

set CLASSPATH=%DOCPORTAL_HOME%\build\lib\third-party.jar

set TMP1=SQLListHSQLDB1.tmp
set TMP2=SQLListHSQLDB2.tmp
set TMP3=SQLListHSQLDB3.tmp
set TMP4=SQLListHSQLDB4.tmp

echo select COUNT(MCRID) from %1 where ( MCRID like '%2%%'); > %TMP1%

%JAVA_HOME%/bin/java -Xmx256m org.hsqldb.util.SqlTool --rcfile sqltool.rc localhost-sa < %TMP1% > %TMP2%
type %TMP2% | gnutools\bin\grep sql > %TMP3%
type %TMP3% | gnutools\bin\mawk {printf($2)} > %TMP4%
type %TMP4%

del %TMP1%
del %TMP2%
del %TMP3%
del %TMP4%