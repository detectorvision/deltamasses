#!/usr/local/bin/perl
###############################################################################
#Copyright by Detectorvision AG Zurich
#detectorvision_export.pl
#perl C:\detectorvision\deltaMasses\bin\detectorvision_export.pl  <resultfilepath> <datafilepath> <datafilename>
###############################################################################
use LWP::Simple;

#CONFIGURATION############################################################
#URL to your mascot server (e.g. http://www.mycompany.com/mascot)
my $MASCOT_SERVER = 'http://localhost/mascot';
#Logfile for this script. 
my $LOGFILE='C:/detectorvision/deltaMasses/log/mascot_exporter.log';
#Directory to where the .mgf and .xml files are exported
my $MASCOT_EXPORT_DIRECTORY='C:/detectorvision/deltaMasses/mascot_export';
###############################################################################
my $MASCOT_SERVER_CGI = $MASCOT_SERVER . '/cgi/';


#SYSTEM CHECK##################################################################
open(LOG,">>$LOGFILE") or die "cannot open logfile $LOGFILE\n";
print LOG "------------------------------------------------------------\n";
my $date=localtime(time);
print LOG "$date :: logfile $LOGFILE opened\n";

if(! -e $MASCOT_EXPORT_DIRECTORY){
	print "logfile $MASCOT_EXPORT_DIRECTORY does not exist, exit\n";
	print LOG "export directory $MASCOT_EXPORT_DIRECTORY does not exist, exit\n";
	exit(0);
}
###############################################################################
my $resultfilepath=$ARGV[0];
print LOG "argument vector:\n@ARGV\n";
print "argument vector:\n@ARGV\n";
print LOG "ARGV[0] is::$ARGV[0]::\n";
print LOG "ARGV[1] is::$ARGV[1]::\n";
print LOG "ARGV[2] is::$ARGV[2]::\n";

#determination of target names (XMl and MGF)###################################
my $filebase= $MASCOT_EXPORT_DIRECTORY .'/';
my $tmp=$ARGV[0];
$tmp=~s/.*\///;     #remove all until and including last slash
$tmp=~s/\.dat//;    #remove trailing .dat
print LOG "tmp=$tmp\n";

my $tmp2=$ARGV[2]; #.mgf filename
$tmp2=~s/\..*//;#remove all after a point, including the point (fileending)
$filebase = $filebase . $tmp ."_".$tmp2;
print LOG "filebase:$filebase\n";

my $outFileMGF=$filebase.".mgf";
my $outFileXML=$filebase.".xml";
print LOG "outFileMGF:$outFileMGF\n";
print LOG "outFileXML:$outFileXML\n";
#construct the URL Request#####################################################
$resultfilepath=$ARGV[0];
print "resultfilePath::$resultfilepath::\n";
print LOG "resultfilePath::$resultfilepath::\n";

my $urlRequest1= $MASCOT_SERVER_CGI."export_dat_2.pl?file=$resultfilepath&do_export=1&prot_hit_num=1&prot_acc=1&pep_query=1&pep_rank=1&pep_isbold=1&pep_isunique=1&pep_exp_mz=1&export_format=MGF";
my $urlRequest2= $MASCOT_SERVER_CGI."export_dat_2.pl?file=$resultfilepath&do_export=1&prot_hit_num=1&prot_acc=1&pep_query=1&pep_rank=1&pep_isbold=1&pep_isunique=1&pep_exp_mz=1&export_format=XML&_sigthreshold=0.05&report=AUTO&_server_mudpit_switch=0.000000001&search_master=1&show_header=1&show_mods=1&show_params=1&show_format=1&protein_master=1&prot_score=1&prot_desc=1&prot_mass=1&prot_matches=1&peptide_master=1&pep_exp_mr=1&pep_exp_z=1&pep_calc_mr=1&pep_delta=1&pep_miss=1&pep_score=1&pep_expect=1&pep_seq=1&pep_var_mod=1&pep_scan_title=1";

print "urlRequest1: $urlRequest1\n";
print "urlRequest2: $urlRequest2\n";

#call the Mascot server and store results in $content variables################
my $content1 = get $urlRequest1;
die "Couldn't get $urlRequest1" unless defined $content1;
print LOG "fetching url1 OK\n";

my $content2 = get $urlRequest2;
die "Couldn't get $urlRequest2" unless defined $content2;
print LOG "fetching url2 OK\n";

print LOG "all Ok, returning\n";
print "all Ok, returning\n";

#write contents to the files###################################################
open(OUTMGF,">$outFileMGF") or die "cannot write to $outFileMGF";
print OUTMGF $content1;
close(OUTMGF);
print LOG "wrote $outFileMGF\n";

open(OUTXML,">$outFileXML") or die "cannot write to $outFileXML";
print OUTXML $content2;
close(OUTXML);
print LOG "wrote $outFileXML\n";

close(LOG);
exit(0);