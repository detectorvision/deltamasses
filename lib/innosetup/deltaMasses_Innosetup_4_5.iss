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

;============================================
;configuration
;============================================
;no configuration yet

[Setup]
AppName=deltaMasses
AppVerName=deltaMasses v 4.5 build #583
AppPublisher=Detectorvision AG
AppPublisherURL=http://www.detectorvision.com
AppSupportURL=http://www.detectorvision.com
AppUpdatesURL=http://www.detectorvision.com
DefaultDirName=C:\detectorvision\deltaMasses\
DisableDirPage=yes
DefaultGroupName=DETECTORVISION
DisableProgramGroupPage=yes
LicenseFile=C:\Users\frank\workspace\deltaMasses\dist\documentation\license.txt
InfoBeforeFile=C:\Users\frank\workspace\deltaMasses\dist\documentation\before_installation.txt
InfoAfterFile=C:\Users\frank\workspace\deltaMasses\dist\documentation\after_installation.txt
OutputBaseFilename=deltaMasses_v_4_5_b583_rc3
SetupIconFile=C:\Users\frank\workspace\deltaMasses\dist\images\detectorvision_transparent_install.ico
Password=discoverPTM

;harder compression since build 60.
;Compression=none
Compression=lzma
;Compression=LZMA/Ultra
SolidCompression=true

;SolidCompression=yes
;new build 60
;AppCopyright=Copyright © 2005-2010 Detectorvision AG Zurich
;WindowVisible=true
;BackColor=$FFFFFF
;BackColor2=$FFFFFF
WizardImageFile=C:\Users\frank\workspace\deltaMasses\dist\images\protein.bmp
WizardSmallImageFile=C:\Users\frank\workspace\deltaMasses\dist\images\protein_small.bmp

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Messages]
BeveledLabel=© 2005-2010 Detectorvision AG Zurich

[Dirs]
;Name: {app}\automation\cluster
Name: {app}\license
Name: {app}\documentation
Name: {app}\bin
Name: {app}\images
Name: {app}\data
Name: {app}\config
Name: {app}\log
;   {app}\log\automation.log.txt     written by the application, NOT distributed
Name: {app}\automation
Name: {app}\tmp
Name: {app}\automation\mascot
; mascot searches.logfile goes to the above directory
; mascot.server.txt goes to abvoe directory (written by application, NOT distributed)
Name: {app}\automation\mascot\data
;mascot.xml files go to the data directory (no subdirectories)
Name: {app}\automation\mgf
Name: {app}\automation\mgf\data
Name: {app}\postgreSQL
;Name: {app}\deltaCluster\bin






[Files]
;development eclipse doesnt work without this ... weird ...20070817
Source: "C:\root3\detectorvision\executable\bin\swt-win32-3232.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\";  Flags: ignoreversion
;infrastructure----------------------------------------------------------------------------------------------
;Source: "c:\root3\installer\bin\java\list.pl"; DestDir: "{app}\bin\java\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\tmp"; DestDir: "{app}\bin\java\"; Flags: ignoreversion

Source: "c:\root3\installer\bin\java\jre1.6.0_02\COPYRIGHT"; DestDir: "{app}\bin\java\jre1.6.0_02\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\LICENSE"; DestDir: "{app}\bin\java\jre1.6.0_02\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\list.pl"; DestDir: "{app}\bin\java\jre1.6.0_02\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\PATCH.ERR"; DestDir: "{app}\bin\java\jre1.6.0_02\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\README.txt"; DestDir: "{app}\bin\java\jre1.6.0_02\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\THIRDPARTYLICENSEREADME.txt"; DestDir: "{app}\bin\java\jre1.6.0_02\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\Welcome.html"; DestDir: "{app}\bin\java\jre1.6.0_02\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\awt.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\axbridge.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\cmm.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\dcpr.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\deploy.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\dt_shmem.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\dt_socket.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\fontmanager.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\hpi.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\hprof.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\instrument.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\ioser12.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\j2pcsc.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\j2pkcs11.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jaas_nt.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\java-rmi.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\java.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\java.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\javacpl.cpl"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\javacpl.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\javaw.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\javaws.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\java_crw_demo.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jawt.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\JdbcOdbc.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jdwp.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jli.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jpeg.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jpicom.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jpiexp.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jpinscp.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jpioji.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jpishare.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jsound.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jsoundds.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jucheck.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jureg.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\jusched.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\keytool.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\kinit.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\klist.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\ktab.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\management.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\msvcr71.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\net.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\nio.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\npjava11.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\npjava12.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\npjava13.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\npjava14.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\npjava32.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\npjpi160_02.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\npoji610.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\npt.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\orbd.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\pack200.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\policytool.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\regutils.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\rmi.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\rmid.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\rmiregistry.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\servertool.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\splashscreen.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\ssv.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\sunmscapi.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\tnameserv.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\unpack.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\unpack200.exe"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\verify.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\w2k_lsa_auth.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\wsdetect.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\zip.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\client\classes.jsa"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\client\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\client\jvm.dll"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\client\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\bin\client\Xusage.txt"; DestDir: "{app}\bin\java\jre1.6.0_02\bin\client\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\calendars.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\charsets.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\classlist"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\content-types.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\flavormap.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\fontconfig.98.bfc"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\fontconfig.98.properties.src"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\fontconfig.bfc"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\fontconfig.properties.src"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\javaws.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\jce.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\jsse.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\jvm.hprof.txt"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\logging.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\management-agent.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\meta-index"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\net.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\plugin.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\psfont.properties.ja"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\psfontj2d.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\resources.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\rt.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\sound.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\tzmappings"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\cmm\CIEXYZ.pf"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\cmm\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\cmm\GRAY.pf"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\cmm\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\cmm\LINEAR_RGB.pf"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\cmm\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\cmm\sRGB.pf"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\cmm\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\ffjcext.zip"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_de.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_es.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_fr.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_it.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_ja.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_ko.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_sv.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_zh_CN.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_zh_HK.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\messages_zh_TW.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\deploy\splash.jpg"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\deploy\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\ext\dnsns.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\ext\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\ext\localedata.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\ext\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\ext\meta-index"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\ext\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\ext\sunjce_provider.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\ext\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\ext\sunmscapi.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\ext\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\ext\sunpkcs11.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\ext\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\fonts\LucidaSansRegular.ttf"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\fonts\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\i386\jvm.cfg"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\i386\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\im\indicim.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\im\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\im\thaiim.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\im\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\images\cursors\cursors.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\images\cursors\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\images\cursors\invalid32x32.gif"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\images\cursors\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\images\cursors\win32_CopyDrop32x32.gif"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\images\cursors\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\images\cursors\win32_CopyNoDrop32x32.gif"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\images\cursors\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\images\cursors\win32_LinkDrop32x32.gif"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\images\cursors\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\images\cursors\win32_LinkNoDrop32x32.gif"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\images\cursors\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\images\cursors\win32_MoveDrop32x32.gif"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\images\cursors\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\images\cursors\win32_MoveNoDrop32x32.gif"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\images\cursors\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\management\jmxremote.access"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\management\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\management\jmxremote.password.template"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\management\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\management\management.properties"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\management\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\management\snmp.acl.template"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\management\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\security\cacerts"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\security\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\security\java.policy"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\security\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\security\java.security"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\security\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\security\javaws.policy"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\security\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\security\local_policy.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\security\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\security\US_export_policy.jar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\security\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\CET"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\CST6CDT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\EET"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\EST"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\EST5EDT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\GMT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\HST"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\MET"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\MST"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\MST7MDT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\PST8PDT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\WET"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\ZoneInfoMappings"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Abidjan"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Accra"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Addis_Ababa"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Algiers"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Asmara"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Bamako"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Bangui"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Banjul"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Bissau"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Blantyre"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Brazzaville"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Bujumbura"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Cairo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Casablanca"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Ceuta"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Conakry"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Dakar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Dar_es_Salaam"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Djibouti"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Douala"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\El_Aaiun"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Freetown"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Gaborone"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Harare"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Johannesburg"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Kampala"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Khartoum"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Kigali"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Kinshasa"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Lagos"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Libreville"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Lome"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Luanda"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Lubumbashi"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Lusaka"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Malabo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Maputo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Maseru"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Mbabane"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Mogadishu"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Monrovia"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Nairobi"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Ndjamena"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Niamey"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Nouakchott"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Ouagadougou"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Porto-Novo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Sao_Tome"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Tripoli"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Tunis"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Africa\Windhoek"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Africa\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Adak"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Anchorage"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Anguilla"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Antigua"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Araguaina"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Aruba"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Asuncion"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Atikokan"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Bahia"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Barbados"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Belem"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Belize"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Blanc-Sablon"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Boa_Vista"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Bogota"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Boise"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Cambridge_Bay"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Campo_Grande"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Cancun"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Caracas"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Cayenne"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Cayman"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Chicago"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Chihuahua"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Costa_Rica"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Cuiaba"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Curacao"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Danmarkshavn"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Dawson"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Dawson_Creek"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Denver"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Detroit"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Dominica"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Edmonton"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Eirunepe"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\El_Salvador"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Fortaleza"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Glace_Bay"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Godthab"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Goose_Bay"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Grand_Turk"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Grenada"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Guadeloupe"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Guatemala"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Guayaquil"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Guyana"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Halifax"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Havana"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Hermosillo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Inuvik"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Iqaluit"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Jamaica"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Juneau"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\La_Paz"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Lima"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Los_Angeles"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Maceio"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Managua"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Manaus"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Martinique"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Mazatlan"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Menominee"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Merida"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Mexico_City"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Miquelon"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Moncton"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Monterrey"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Montevideo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Montreal"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Montserrat"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Nassau"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\New_York"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Nipigon"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Nome"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Noronha"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Panama"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Pangnirtung"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Paramaribo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Phoenix"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Port-au-Prince"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Porto_Velho"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Port_of_Spain"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Puerto_Rico"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Rainy_River"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Rankin_Inlet"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Recife"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Regina"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Resolute"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Rio_Branco"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Santiago"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Santo_Domingo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Sao_Paulo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Scoresbysund"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\St_Johns"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\St_Kitts"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\St_Lucia"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\St_Thomas"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\St_Vincent"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Swift_Current"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Tegucigalpa"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Thule"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Thunder_Bay"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Tijuana"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Toronto"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Tortola"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Vancouver"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Whitehorse"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Winnipeg"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Yakutat"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Yellowknife"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\Buenos_Aires"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\Catamarca"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\Cordoba"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\Jujuy"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\La_Rioja"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\Mendoza"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\Rio_Gallegos"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\San_Juan"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\Tucuman"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Argentina\Ushuaia"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Argentina\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Indiana\Indianapolis"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Indiana\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Indiana\Knox"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Indiana\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Indiana\Marengo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Indiana\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Indiana\Petersburg"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Indiana\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Indiana\Vevay"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Indiana\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Indiana\Vincennes"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Indiana\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Indiana\Winamac"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Indiana\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Kentucky\Louisville"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Kentucky\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\Kentucky\Monticello"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\Kentucky\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\North_Dakota\Center"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\North_Dakota\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\America\North_Dakota\New_Salem"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\America\North_Dakota\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Antarctica\Casey"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Antarctica\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Antarctica\Davis"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Antarctica\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Antarctica\DumontDUrville"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Antarctica\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Antarctica\Mawson"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Antarctica\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Antarctica\McMurdo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Antarctica\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Antarctica\Palmer"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Antarctica\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Antarctica\Rothera"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Antarctica\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Antarctica\Syowa"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Antarctica\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Antarctica\Vostok"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Antarctica\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Aden"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Almaty"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Amman"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Anadyr"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Aqtau"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Aqtobe"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Ashgabat"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Baghdad"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Bahrain"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Baku"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Bangkok"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Beirut"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Bishkek"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Brunei"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Calcutta"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Choibalsan"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Chongqing"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Colombo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Damascus"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Dhaka"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Dili"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Dubai"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Dushanbe"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Gaza"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Harbin"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Hong_Kong"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Hovd"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Irkutsk"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Jakarta"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Jayapura"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Jerusalem"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Kabul"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Kamchatka"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Karachi"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Kashgar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Katmandu"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Krasnoyarsk"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Kuala_Lumpur"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Kuching"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Kuwait"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Macau"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Magadan"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Makassar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Manila"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Muscat"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Nicosia"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Novosibirsk"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Omsk"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Oral"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Phnom_Penh"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Pontianak"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Pyongyang"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Qatar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Qyzylorda"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Rangoon"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Riyadh"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Riyadh87"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Riyadh88"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Riyadh89"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Saigon"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Sakhalin"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Samarkand"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Seoul"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Shanghai"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Singapore"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Taipei"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Tashkent"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Tbilisi"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Tehran"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Thimphu"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Tokyo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Ulaanbaatar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Urumqi"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Vientiane"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Vladivostok"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Yakutsk"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Yekaterinburg"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Asia\Yerevan"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Asia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\Azores"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\Bermuda"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\Canary"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\Cape_Verde"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\Faroe"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\Madeira"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\Reykjavik"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\South_Georgia"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\Stanley"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Atlantic\St_Helena"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Atlantic\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Adelaide"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Brisbane"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Broken_Hill"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Currie"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Darwin"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Eucla"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Hobart"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Lindeman"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Lord_Howe"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Melbourne"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Perth"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Australia\Sydney"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Australia\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+1"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+10"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+11"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+12"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+2"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+3"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+4"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+5"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+6"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+7"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+8"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT+9"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-1"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-10"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-11"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-12"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-13"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-14"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-2"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-3"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-4"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-5"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-6"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-7"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-8"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\GMT-9"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\UCT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Etc\UTC"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Etc\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Amsterdam"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Andorra"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Athens"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Belgrade"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Berlin"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Brussels"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Bucharest"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Budapest"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Chisinau"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Copenhagen"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Dublin"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Gibraltar"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Helsinki"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Istanbul"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Kaliningrad"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Kiev"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Lisbon"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\London"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Luxembourg"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Madrid"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Malta"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Minsk"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Monaco"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Moscow"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Oslo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Paris"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Prague"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Riga"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Rome"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Samara"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Simferopol"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Sofia"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Stockholm"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Tallinn"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Tirane"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Uzhgorod"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Vaduz"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Vienna"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Vilnius"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Volgograd"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Warsaw"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Zaporozhye"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Europe\Zurich"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Europe\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Antananarivo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Chagos"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Christmas"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Cocos"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Comoro"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Kerguelen"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Mahe"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Maldives"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Mauritius"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Mayotte"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Indian\Reunion"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Indian\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Apia"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Auckland"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Chatham"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Easter"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Efate"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Enderbury"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Fakaofo"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Fiji"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Funafuti"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Galapagos"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Gambier"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Guadalcanal"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Guam"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Honolulu"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Johnston"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Kiritimati"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Kosrae"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Kwajalein"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Majuro"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Marquesas"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Midway"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Nauru"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Niue"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Norfolk"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Noumea"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Pago_Pago"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Palau"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Pitcairn"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Ponape"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Port_Moresby"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Rarotonga"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Saipan"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Tahiti"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Tarawa"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Tongatapu"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Truk"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Wake"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\Pacific\Wallis"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\Pacific\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\AST4"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\AST4ADT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\CST6"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\CST6CDT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\EST5"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\EST5EDT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\HST10"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\MST7"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\MST7MDT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\PST8"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\PST8PDT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\YST9"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion
Source: "c:\root3\installer\bin\java\jre1.6.0_02\lib\zi\SystemV\YST9YDT"; DestDir: "{app}\bin\java\jre1.6.0_02\lib\zi\SystemV\"; Flags: ignoreversion


;bin files

Source: "C:\Users\frank\workspace\deltaMasses\dist\deltamasses.bat"; DestDir: "{app}\"; Flags: ignoreversion

Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\deltaMasses.jar"; DestDir: "{app}\lib"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\config\log4j.properties"; DestDir: "{app}\config"; Flags: ignoreversion

Source: "C:\root3\detectorvision\executable\bin\swt-win32-3232.dll"; DestDir: "{app}\dist\lib"; Flags: ignoreversion
Source: "C:\root3\installer\bin\swt-win32-3139.dll"; DestDir: "{app}\dist\lib"; Flags: ignoreversion

;license and install text
Source: "C:\Users\frank\workspace\deltaMasses\dist\documentation\before_installation.txt"; DestDir: "{app}\documentation"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\documentation\after_installation.txt"; DestDir: "{app}\documentation"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\documentation\license.txt"; DestDir: "{app}\license"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\documentation\registration.txt"; DestDir: "{app}\license\"; Flags: ignoreversion

;images
Source: "C:\root3\installer\images\detectorvision.ico"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\root3\installer\images\detectorvision_transparent.ico"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\root3\installer\images\detectorvision.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\root3\installer\images\blue_bar.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\root3\installer\images\deltaMasses.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\root3\installer\images\deltaProtein.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\root3\detectorvision\deltaMasses\src\images\about_image.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "c:\Users\frank\workspace\deltaMasses\lib\innosetup\detectorvision_icon.gif"; DestDir: "{app}\dist\images\"; Flags: ignoreversion
Source: "c:\Users\frank\workspace\deltaMasses\src\com\detectorvision\images\start_background_spectrum_canvas.png"; DestDir: "{app}\images\"; Flags: ignoreversion



Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\unimod_logo.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\16-arrow-next.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\16-control-pause.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\16-control-stop.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\16-settings.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\16-tag-add.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-arrow-next.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-control-pause.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-control-stop.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-em-move.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-message-info.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-settings.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-tab-open.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-tools.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-zoom-fill.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-zoom-in.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-zoom-out.png"; DestDir: "{app}\images\"; Flags: ignoreversion

Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-on.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\24-off.png"; DestDir: "{app}\images\"; Flags: ignoreversion

Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\detectorvision_transparent.png"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\pmc_image.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\dmb_image.png"; DestDir: "{app}\images\"; Flags: ignoreversion
;;Source: "C:\Users\frank\workspace\deltaMasses\bin\com\detectorvision\images\mdh_image.png"; DestDir: "{app}\images\"; Flags: ignoreversion


Source: "C:\Users\frank\workspace\deltaMasses\lib\innosetup\uninstall_icon.ico"; DestDir: "{app}\images\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\lib\innosetup\pdf_ico.ico"; DestDir: "{app}\images\"; Flags: ignoreversion

;documentation files
Source: "C:\Users\frank\workspace\deltaMasses\dist\documentation\deltaMasses_manual_4_5.pdf"; DestDir: "{app}\documentation\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\documentation\deltaMassBase_Installation.pdf"; DestDir: "{app}\documentation\"; Flags: ignoreversion

;config files
Source: "C:\Users\frank\workspace\deltaMasses\dist\config\unimod.xml"; DestDir: "{app}\config\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\config\preferences.xsd"; DestDir: "{app}\config\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\config\preferences.xml"; DestDir: "{app}\config\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\config\mascot_search_results_1.xsd"; DestDir: "{app}\config\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\config\mascot_search_results_2.xsd"; DestDir: "{app}\config\"; Flags: ignoreversion

;ICPL configuration file
;Source: "C:\root3\detectorvision\executable\config\ICPL_spot2retention.tsv"; DestDir: "{app}\config\"; Flags: ignoreversion

;all .jar files in bin\lib
;new in version 0.8
;Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\algorithms.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\massspectrometry.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\jdom.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\kxml2-2.3.0.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
;Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\org.eclipse.swt.win32.win32.x86_3.2.0.v3232m.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\xmlpull_1_1_3_4b.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\xswt.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\commons-math-1.1.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
;Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\peakfileloaders.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion

Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\images.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\utility.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\itext-1.4.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\jcommon-1.0.12.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\log4j-1.2.15.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\postgresql-8.2-504.jdbc2.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\swt-3.3.2.jar"; DestDir: "{app}\lib\"; Flags: ignoreversion
;Source: "C:\Users\frank\workspace\deltaMasses\dist\lib\"; DestDir: "{app}\lib\"; Flags: ignoreversion

;postgres documentation
Source: "C:\Users\frank\workspace\deltaMasses\dist\documentation\pgadmin3.chm"; DestDir: "{app}\documentation"; Flags: ignoreversion
Source: "C:\Users\frank\workspace\deltaMasses\dist\documentation\postgresql-8.2-A4.pdf"; DestDir: "{app}\documentation"; Flags: ignoreversion

;example data files
Source: "C:\root3\installer\data\example.mgf"; DestDir: "{app}\data\"; Flags: ignoreversion
Source: "C:\root3\installer\data\example.xt.xml"; DestDir: "{app}\data\"; Flags: ignoreversion
Source: "C:\root3\installer\data\example.mascot.xml"; DestDir: "{app}\data\"; Flags: ignoreversion
;Source: "C:\root3\installer\data\sample.random.params"; DestDir: "{app}\data\"; Flags: ignoreversion
;new build 62
;Source: "C:\root3\installer\data\SMC4_HUMAN.random.params.mgf"; DestDir: "{app}\data\"; Flags: ignoreversion
;Source: "C:\root3\installer\data\SMC4_HUMAN.random.params.xt.xml"; DestDir: "{app}\data\"; Flags: ignoreversion
;Source: "C:\root3\installer\data\SMC4_HUMAN.random.params"; DestDir: "{app}\data\"; Flags: ignoreversion
;Source: "C:\root3\installer\data\SMC4_HUMAN.random.params.mascot.xml"; DestDir: "{app}\data\"; Flags: ignoreversion
;Source: "C:\root3\installer\data\QTOF.mascot.xml"; DestDir: "{app}\data\"; Flags: ignoreversion

Source: "C:\Users\frank\workspace\deltaMasses\dist\config\deltaMassBase.config.txt"; DestDir: "{app}\config\"; Flags: ignoreversion

;postgreSQL database
Source: "C:\root3\installer\postgreSQL\postgresql-8.2-int.msi"; DestDir: "{app}\postgreSQL\"; Flags: ignoreversion
Source: "C:\root3\installer\postgreSQL\postgresql-8.2.msi"; DestDir: "{app}\postgreSQL\"; Flags: ignoreversion
Source: "C:\root3\installer\postgreSQL\README.TXT"; DestDir: "{app}\postgreSQL\"; Flags: ignoreversion

[Icons]
;main shortcuts
;Name: "{userdesktop}\deltaMasses"; Filename: {app}\bin\java\jre1.6.0_02\bin\javaw.exe; WorkingDir: {app}\bin\; IconFilename: {app}\images\detectorvision_transparent.ico; IconIndex: 0; Parameters: " -Xms400m -Xmx400m -jar {app}\bin\deltaMasses.jar"
Name: "{group}\deltaMasses_V_4_5"; Filename: {app}\deltamasses.bat; WorkingDir: {app}\; IconFilename: {app}\images\detectorvision_transparent.ico; IconIndex: 0;
;Name: "{group}\deltaProtein"; Filename: {app}\bin\java\jre1.6.0_02\bin\javaw.exe; WorkingDir: {app}\deltaProtein\dist\; IconFilename: {app}\images\detectorvision_transparent.ico; IconIndex: 0; Parameters: " -Xms400m -Xmx400m -cp postgresql-8.2-506.jdbc3.jar -jar deltaProtein.jar jdbc:postgresql://localhost/deltaMassBase postgres 4.3.jjMM"
;Name: "{group}\deltaCluster\export_2_deltaCluster"; Filename: {app}\bin\java\jre1.6.0_02\bin\java.exe; WorkingDir: {app}\deltaCluster\bin\; Parameters: " -Xms250m -Xmx250m -jar deltaMassBase2Cluster.jar export"
;Name: "{group}\deltaCluster\import_from_deltaCluster"; Filename: {app}\bin\java\jre1.6.0_02\bin\java.exe; WorkingDir: {app}\deltaCluster\bin\; Parameters: " -Xms250m -Xmx250m -jar deltaMassBase2Cluster.jar import"

Name: "{group}\deltaMasses manual"; Filename: "{app}\documentation\deltaMasses_manual_4_5.pdf";  IconFilename:{app}\images\pdf_ico.ico;
;Name: "{group}\deltaProtein manual"; Filename: "{app}\documentation\deltaProtein_manual.pdf";  IconFilename:{app}\images\pdf_ico.ico;
Name: "{group}\homepage"; Filename: "http://www.detectorvision.com/deltaMasses.html";
Name: "{group}\Uninstall"; Filename: "{app}\unins000.exe"; IconFilename:{app}\images\uninstall_icon.ico;

;Name: "{group}\ICPL\Toplab"; Filename: "http://www.toplab.de/ICPL.html"



[Run]
;Filename: msiexec; WorkingDir: {app}\postgreSQL; Parameters: "/i postgresql-8.2-int.msi /qr INTERNALLAUNCH=1 ADDLOCAL=server,psql,pgadmin,jdbc,nls,npqsgl,docs SERVICEDOMAIN='%COMPUTERNAME%' SERVICEPASSWORD='44jkl23456lkj' SUPERPASSWORD='...Vfkl4isf9ef'"; Flags: shellexec;
;Disabled autostart due to http://www.jrsoftware.org/iskb.php?vista
;Filename: "{app}\documentation\deltaMasses_manual.pdf"; Flags: shellexec postinstall skipifsilent;
;Disabled autostart below 20061220
;Filename: javaw.exe; WorkingDir: {app}\bin\; Parameters: "  -Xms250m -Xmx250m -jar {app}\bin\deltaMasses.jar"; Flags: postinstall skipifsilent;
;Filename: "{app}\bin\deltaMasses.bat"; Description: "{cm:LaunchProgram,deltaMasses}"; Flags: shellexec postinstall skipifsilent
