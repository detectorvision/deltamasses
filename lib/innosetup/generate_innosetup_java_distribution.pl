#!/usr/bin/perl

$base_path="C:\\root_install_java_v5";



process_files ($base_path);

# Accepts one argument: the full path to a directory.
# Returns: nothing.
sub process_files {
    my $path = shift;

    # Open the directory.
    opendir (DIR, $path)
        or die "Unable to open $path: $!";

    # Read in the files.
    # You will not generally want to process the '.' and '..' files,
    # so we will use grep() to take them out.
    # See any basic Unix filesystem tutorial for an explanation of them.
    my @files = grep { !/^\.{1,2}$/ } readdir (DIR);

    # Close the directory.
    closedir (DIR);

    # At this point you will have a list of filenames
    #  without full paths ('filename' rather than
    #  '/home/count0/filename', for example)
    # You will probably have a much easier time if you make
    #  sure all of these files include the full path,
    #  so here we will use map() to tack it on.
    #  (note that this could also be chained with the grep
    #   mentioned above, during the readdir() ).
    @files = map { $path . '/' . $_ } @files;

    for (@files) {

        # If the file is a directory
        if (-d $_) {
            # Here is where we recurse.
            # This makes a new call to process_files()
            # using a new directory we just found.
            process_files ($_);

        # If it isn't a directory, lets just do some
        # processing on it.
        } else { 
             #Source: "c:\root3\installer\bin\java\jre1.6.0_02\COPYRIGHT"; DestDir: "{app}\bin\java\jre1.6.0_02\"; Flags: ignoreversion
             $dest=$path;
             $dest=~ s/C:\\root_install_java_v5//;#siehe base_path oben, muss entfernt werden.
             $dest=~ s/\//\\/g;#alle /-slashes umdrehen
            
             $file=$_;
             $file=~ s/\//\\/g;#alle /-slashes umdrehen
             
             if( ! ($file=~/\\sample\\/)  &&  ! ($file=~/\\demo\\/) ){#muell rausfiltern
	        print "Source: \"$file\"; DestDir:\"{app}\\bin$dest\"; Flags: ignoreversion\n";
             }
        }
    }
}
