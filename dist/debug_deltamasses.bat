rem ----------------------------------------
rem
rem   Protein Science Tools from Switzerland
rem 
rem   Differential PTM Detection
rem
rem   Detectorvision AG, Zurich
rem
rem   same as deltamasses.bat but java instead of javaw
rem ----------------------------------------
rem @echo off
 set JAVAEXE=bin\java\jdk1.6.0_22\bin\javaw.exe
set CP=.
rem Detectorvision generated jars
set CP=%CP%;lib\deltamasses.jar
set CP=%CP%;lib\images.jar
set CP=%CP%;lib\massspectrometry.jar
set CP=%CP%;lib\utility.jar
rem External jars
set CP=%CP%;lib\commons-math-1.1.jar
set CP=%CP%;lib\itext-1.4.jar
set CP=%CP%;lib\jcommon-1.0.12.jar
set CP=%CP%;lib\jdom.jar
set CP=%CP%;lib\jfreechart-1.0.9.jar
set CP=%CP%;lib\kxml2-2.3.0.jar
set CP=%CP%;lib/log4j-1.2.15.jar
set CP=%CP%;lib\postgresql-8.2-504.jdbc2.jar
set CP=%CP%;lib\xmlpull_1_1_3_4b.jar
set CP=%CP%;lib\xswt.jar
set CP=%CP%;"bin\java\jdk1.6.0_22\bin\swt.jar" 
            

%JAVAEXE% -Xmx512M -Xms512M -cp %CP% com.detectorvision.deltaMasses.DeltaMasses -Dlog4j.configuration=file:"config\log4j.properties" 


