@echo off

rem
rem Repair all search collections and extended tables with data from the 
rem XML SQL store
rem

%DOCPORTAL_HOME%\build\bin\mycore.cmd repair metadata search of type institution
%DOCPORTAL_HOME%\build\bin\mycore.cmd repair metadata search of type author
%DOCPORTAL_HOME%\build\bin\mycore.cmd repair metadata search of type document

rem %DOCPORTAL_HOME%\build\bin\mycore.cmd repair derivate search of type derivate
