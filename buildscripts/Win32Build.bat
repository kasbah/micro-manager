\bin\pskill javaw.exe
CALL buildscripts\updateallfromsvn.bat
CALL buildscripts\MMBUILD.BAT withpython
pscp -i c:\projects\MM.ppk -batch /projects/micromanager1.3/Install/Output/MMSetup_%mmversion%_%YYYYMMDD%.exe MM@valelab.ucsf.edu:./public_html/nightlyBuilds/1.3/Windows/MMSetup_%mmversion%_%YYYYMMDD%.exe
