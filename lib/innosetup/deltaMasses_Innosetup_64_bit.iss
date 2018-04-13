;============================================
;deltaMasses TM InnoSetup Compile Script
;All rights reserved by
;Detectorvision AG
;Keltenstrasse 10
;CH 8044 Zürich
;
;============================================
;       VERSION HISTORY
;============================================
;v 0.6 20060906
;
;============================================
;v 0.6.1 20060907                                                              .
;added directory {app}/documentation to directory.
;moved license files to {app}/documentation directory.
;example dataset now Orbitrap_Yeast.mgf
;============================================
;v 0.6.2 20060909
;added  deltaMasses_manual.pdf and shortcuts to it.
;============================================
;v 0.7.0 20060912
;added  Web Resources links
;added preferences.xsd preferences.xml
;============================================
;v 0.8.0 20060914
;changed to new structure by michael
;
;============================================
;v 1.1.0 20061013
;First test release
;v 1.1.0.1 20061014   added new unimod.xml file
;v 1.1.0.2 20061017   added file data/sample.random.params
;v 1.1.0.3 20061022   removed Orbitrap_Yeast.mgf
;                     added orbitrap.mgf
;                     changed icon shortcuts and postinstall commands as described on
;                     http://www.inno-setup.de/showthread.php?p=4562#post4562
;============================================
;v 2.0 20070121  included Sun JRE for redistribution
;============================================
;  64 bit version 20101208 

;============================================
;configuration
;============================================
;no configuration yet

[Setup]
;INTEGRATION_CONTROL check the whole setup-block
AppName=deltaMasses
AppVerName=deltaMasses v 5.2 build #787         
AppPublisher=Detectorvision AG
AppPublisherURL=http://www.detectorvision.com
AppSupportURL=http://www.detectorvision.com
AppUpdatesURL=http://www.detectorvision.com
DefaultDirName=C:\detectorvision\deltaMasses\
DisableDirPage=yes
DefaultGroupName=DETECTORVISION
DisableProgramGroupPage=yes
LicenseFile=C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\license.txt

InfoBeforeFile=C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\doc\dist\before_installation.txt
InfoAfterFile=C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\doc\dist\after_installation.txt
OutputBaseFilename=deltaMasses_v_5_2_b787_rc_3
SetupIconFile=C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\detectorvision_transparent_install.ico
Password=discovermore

Compression=lzma
SolidCompression=true
WizardImageFile=C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\protein.bmp
WizardSmallImageFile=C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\protein_small.bmp

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Messages]
BeveledLabel=© 2005-2013 Detectorvision AG Zurich

[Dirs]
Name: {app}\automation\cluster
Name: {app}\license
Name: {app}\documentation
Name: {app}\bin
Name: {app}\images
Name: {app}\data
Name: {app}\config
Name: {app}\log
Name: {app}\automation
Name: {app}\tmp
Name: {app}\automation\mascot
; mascot searches.logfile goes to the above directory
; mascot.server.txt goes to abvoe directory (written by application, NOT distributed)
Name: {app}\automation\mascot\data
Name: {app}\automation\mgf
Name: {app}\automation\mgf\data
Name: {app}\postgreSQL
Name: {app}\deltaCluster\bin            

[Files]
;infrastructure----------------------------------------------------------------------------------------------
;;;;;;;;;;;;;;;;;;;;;JAVA DISTRIBUTION COMES HERE
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\appletviewer.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\apt.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\extcheck.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\HtmlConverter.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\idlj.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jar.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jarsigner.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\java-rmi.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\java.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\javac.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\javadoc.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\javah.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\javap.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\javaw.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\javaws.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jconsole.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jdb.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jhat.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jinfo.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jli.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jmap.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jps.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jrunscript.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jstack.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jstat.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jstatd.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\jvisualvm.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\keytool.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\kinit.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\klist.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\ktab.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\native2ascii.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\orbd.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\pack200.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\policytool.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\rmic.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\rmid.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\rmiregistry.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\schemagen.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\serialver.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\servertool.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\swt-awt-win32-3655.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\swt-gdip-win32-3655.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\swt-wgl-win32-3655.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
;Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\swt-win32-3232.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
;Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\swt-win32-3550.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\swt-win32-3655.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\swt.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\tnameserv.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\unpack200.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\wsgen.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\wsimport.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\bin\xjc.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\COPYRIGHT"; DestDir:"{app}\bin\java\jdk1.6.0_22"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\include\classfile_constants.h"; DestDir:"{app}\bin\java\jdk1.6.0_22\include"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\include\jawt.h"; DestDir:"{app}\bin\java\jdk1.6.0_22\include"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\include\jdwpTransport.h"; DestDir:"{app}\bin\java\jdk1.6.0_22\include"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\include\jni.h"; DestDir:"{app}\bin\java\jdk1.6.0_22\include"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\include\jvmti.h"; DestDir:"{app}\bin\java\jdk1.6.0_22\include"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\include\win32\jawt_md.h"; DestDir:"{app}\bin\java\jdk1.6.0_22\include\win32"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\include\win32\jni_md.h"; DestDir:"{app}\bin\java\jdk1.6.0_22\include\win32"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\attach.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\awt.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\cmm.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\dcpr.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\deploy.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\deployJava1.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\dt_shmem.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\dt_socket.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\eula.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\fontmanager.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\hpi.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\hprof.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\instrument.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\ioser12.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\j2pcsc.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jaas_nt.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\java-rmi.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\java.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\java.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\javacpl.cpl"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\javacpl.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\javaw.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\javaws.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\java_crw_demo.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jawt.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jbroker.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\JdbcOdbc.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jdwp.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jli.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jp2iexp.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jp2launcher.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jp2native.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jp2ssv.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jpeg.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\jsound.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\keytool.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\kinit.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\klist.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\ktab.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\management.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\mlib_image.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\msvcrt.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\net.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\new_plugin\msvcrt.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin\new_plugin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\new_plugin\npjp2.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin\new_plugin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\nio.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\npdeployJava1.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\npt.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\orbd.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\pack200.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\policytool.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\regutils.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\rmi.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\rmid.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\rmiregistry.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\server\jvm.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin\server"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\server\Xusage.txt"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin\server"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\servertool.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\splashscreen.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\ssv.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\ssvagent.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\tnameserv.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\unpack.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\unpack200.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\verify.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\w2k_lsa_auth.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\wsdetect.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\bin\zip.dll"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\bin"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\COPYRIGHT"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\alt-rt.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\amd64\jvm.cfg"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\amd64"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\audio\soundbank.gm"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\audio"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\calendars.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\charsets.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\classlist"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\cmm\CIEXYZ.pf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\cmm"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\cmm\GRAY.pf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\cmm"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\cmm\LINEAR_RGB.pf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\cmm"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\cmm\PYCC.pf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\cmm"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\cmm\sRGB.pf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\cmm"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\content-types.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\ffjcext.zip"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_de.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_es.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_fr.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_it.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_ja.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_ko.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_pt_BR.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_sv.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_zh_CN.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_zh_HK.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\messages_zh_TW.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy\splash.gif"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\deploy"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\deploy.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\ext\dnsns.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\ext"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\ext\localedata.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\ext"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\ext\meta-index"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\ext"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\ext\sunjce_provider.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\ext"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\flavormap.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fontconfig.98.bfc"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fontconfig.98.properties.src"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fontconfig.bfc"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fontconfig.properties.src"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fonts\LucidaBrightDemiBold.ttf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\fonts"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fonts\LucidaBrightDemiItalic.ttf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\fonts"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fonts\LucidaBrightItalic.ttf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\fonts"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fonts\LucidaBrightRegular.ttf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\fonts"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fonts\LucidaSansDemiBold.ttf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\fonts"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fonts\LucidaSansRegular.ttf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\fonts"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fonts\LucidaTypewriterBold.ttf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\fonts"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\fonts\LucidaTypewriterRegular.ttf"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\fonts"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\im\indicim.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\im"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\im\thaiim.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\im"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\images\cursors\cursors.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\images\cursors"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\images\cursors\invalid32x32.gif"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\images\cursors"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\images\cursors\win32_CopyDrop32x32.gif"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\images\cursors"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\images\cursors\win32_CopyNoDrop32x32.gif"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\images\cursors"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\images\cursors\win32_LinkDrop32x32.gif"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\images\cursors"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\images\cursors\win32_LinkNoDrop32x32.gif"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\images\cursors"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\images\cursors\win32_MoveDrop32x32.gif"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\images\cursors"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\images\cursors\win32_MoveNoDrop32x32.gif"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\images\cursors"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\javaws.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\jce.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\jsse.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\jvm.hprof.txt"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\launcher.exe"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\logging.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\management\jmxremote.access"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\management"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\management\jmxremote.password.template"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\management"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\management\management.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\management"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\management\snmp.acl.template"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\management"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\management-agent.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\meta-index"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\net.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\plugin.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\psfont.properties.ja"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\psfontj2d.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\resources.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\rt.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\security\blacklist"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\security"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\security\cacerts"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\security"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\security\java.policy"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\security"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\security\java.security"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\security"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\security\javaws.policy"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\security"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\security\local_policy.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\security"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\security\trusted.libraries"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\security"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\security\US_export_policy.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\security"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\servicetag\jdk_header.png"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\servicetag"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\servicetag\registration.xml"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib\servicetag"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\sound.properties"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\lib\tzmappings"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\README.txt"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\THIRDPARTYLICENSEREADME.txt"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\jre\Welcome.html"; DestDir:"{app}\bin\java\jdk1.6.0_22\jre"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\lib\ct.sym"; DestDir:"{app}\bin\java\jdk1.6.0_22\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\lib\dt.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\lib\htmlconverter.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\lib\ir.idl"; DestDir:"{app}\bin\java\jdk1.6.0_22\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\lib\jawt.lib"; DestDir:"{app}\bin\java\jdk1.6.0_22\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\lib\jconsole.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\lib\jvm.lib"; DestDir:"{app}\bin\java\jdk1.6.0_22\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\lib\orb.idl"; DestDir:"{app}\bin\java\jdk1.6.0_22\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\lib\tools.jar"; DestDir:"{app}\bin\java\jdk1.6.0_22\lib"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\README.html"; DestDir:"{app}\bin\java\jdk1.6.0_22"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\README_ja.html"; DestDir:"{app}\bin\java\jdk1.6.0_22"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\README_zh_CN.html"; DestDir:"{app}\bin\java\jdk1.6.0_22"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\register.html"; DestDir:"{app}\bin\java\jdk1.6.0_22"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\register_ja.html"; DestDir:"{app}\bin\java\jdk1.6.0_22"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\register_zh_CN.html"; DestDir:"{app}\bin\java\jdk1.6.0_22"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\src.zip"; DestDir:"{app}\bin\java\jdk1.6.0_22"; Flags: ignoreversion
Source: "C:\root_install_java_v5\java\jdk1.6.0_22\THIRDPARTYLICENSEREADME.txt"; DestDir:"{app}\bin\java\jdk1.6.0_22"; Flags: ignoreversion

;bin files
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\doc\dist\deltamasses.bat"; DestDir: "{app}\"; Flags: ignoreversion
;Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\debug_deltamasses.bat"; DestDir: "{app}\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\deltaMasses.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\config\log4j.properties"; DestDir: "{app}\config"; Flags: ignoreversion

;license and install text
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\documentation\before_installation.txt"; DestDir: "{app}\documentation"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\documentation\after_installation.txt"; DestDir: "{app}\documentation"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\license.txt"; DestDir: "{app}\license"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\registration.txt"; DestDir: "{app}\license\"; Flags: ignoreversion

;images
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\detectorvision.ico"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\detectorvision_transparent.ico"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\detectorvision.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\detectorvision_logo_for_pdf_header.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\blue_bar.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\deltaMasses.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\src\com\detectorvision\images\about_image.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\detectorvision_icon.gif"; DestDir: "{app}\dist\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\src\com\detectorvision\images\start_background_spectrum_canvas.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\unimod_logo.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\16-arrow-next.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\16-control-pause.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\16-control-stop.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\16-tag-add.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\24-arrow-next.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\24-control-stop.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\24-tab-open.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\24-on.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\24-off.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\detectorvision_transparent.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\bin\com\detectorvision\images\pmc_image.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\uninstall_icon.ico"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\lib\innosetup\pdf_ico.ico"; DestDir: "{app}\images\"; Flags: ignoreversion

;documentation files
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\documentation\deltaMasses_manual.pdf"; DestDir: "{app}\documentation\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\documentation\deltaMassBase_Installation.pdf"; DestDir: "{app}\documentation\"; Flags: ignoreversion


;config files
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\config\unimod.xml"; DestDir: "{app}\config\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\config\preferences.xsd"; DestDir: "{app}\config\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\config\preferences.xml"; DestDir: "{app}\config\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\doc\dist\mascot_search_results_1.xsd"; DestDir: "{app}\config\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\doc\dist\mascot_search_results_2.xsd"; DestDir: "{app}\config\"; Flags: ignoreversion

;all .jar files in bin\lib
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\massspectrometry.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\jdom.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\kxml2-2.3.0.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\xmlpull_1_1_3_4b.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\xswt.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\commons-math-1.1.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion

Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\images.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\utility.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\itext-1.4.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\jcommon-1.0.12.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\log4j-1.2.15.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\postgresql-8.2-504.jdbc2.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
;Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\lib\swt-3.3.2.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion


Source: "C:\root_install_java_v5\data\example.mgf"; DestDir: "{app}\data\"; Flags: ignoreversion
Source: "C:\root_install_java_v5\data\example.xt.xml"; DestDir: "{app}\data\"; Flags: ignoreversion
Source: "C:\root_install_java_v5\data\example.mascot.xml"; DestDir: "{app}\data\"; Flags: ignoreversion

Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\config\deltaMassBase.config.txt"; DestDir: "{app}\config\"; Flags: ignoreversion

;postgreSQL database
;Source: "C:\root3\installer\postgreSQL\postgresql-8.2-int.msi"; DestDir: "{app}\postgreSQL\"; Flags: ignoreversion
;Source: "C:\root3\installer\postgreSQL\postgresql-8.2.msi"; DestDir: "{app}\postgreSQL\"; Flags: ignoreversion
;Source: "C:\root3\installer\postgreSQL\README.TXT"; DestDir: "{app}\postgreSQL\"; Flags: ignoreversion

;postgres documentation
;Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\documentation\pgadmin3.chm"; DestDir: "{app}\documentation"; Flags: ignoreversion
;Source: "C:\Users\frank_standard\workspace_deltamasses_2\dmSVN\dist\documentation\postgresql-8.2-A4.pdf"; DestDir: "{app}\documentation"; Flags: ignoreversion

[Icons]
;main shortcuts
Name: "{group}\deltaMasses"; Filename: {app}\deltamasses.bat; WorkingDir: {app}\; IconFilename: {app}\images\detectorvision_transparent.ico; IconIndex: 0;
Name: "{group}\deltaMasses manual"; Filename: "{app}\documentation\deltaMasses_manual.pdf";  IconFilename:{app}\images\pdf_ico.ico;
Name: "{group}\homepage"; Filename: "http://www.detectorvision.com/deltaMasses.html"; IconFilename:{app}\images\pdf_ico.ico;
Name: "{group}\Uninstall"; Filename: "{app}\unins000.exe"; IconFilename:{app}\images\uninstall_icon.ico;

[Run]

