\bin\pskill javaw.exe
CALL buildscripts\updateallfromsvn.bat
CALL buildscriptsx64\MMBUILD.BAT withpython
rem pscp -i c:\projects\MM.ppk -batch /projects/micromanager/Install/Output/MMSetup_%mmversion%_%YYYYMMDD%.exe MM@valelab.ucsf.edu:./public_html/nightlyBuilds/1.4/Windows/MMSetup_%mmversion%_%YYYYMMDD%.exe
