# Changes the prefixes for all Tables, PKs, Indexes, or FKeys from one
# value to another.
#
# Args 
#   (0) Filename_root of the existing 6 (or 4) ddl-sql files from split-ddl-sql-files.pl
#   (1) New Filename_root for the new ddl-sql files.
#   (1) Old Prefix string
#   (2) New Prefix string

# Get the arguments: 
$orig_filename_root = @ARGV[0];   # Filename root of the existing 6 (or 4) ddl-sql files
$new_filename_root = @ARGV[1];    # Filename root for new files
$orig_prefix   = @ARGV[2];        # Original prefix
$new_prefix    = @ARGV[3];        # New prefix

# Get filename root
print "orig_filename_root: ",$orig_filename_root,"\n";
print "new_filename_root:  ",$new_filename_root,"\n";
print "orig_prefix:        ",$orig_prefix,"\n";
print "new_prefix:         ",$new_prefix,"\n";

# Try to open the 6 input files
if (open (CREATE_TABLES_IN, "<".$orig_filename_root.".create-tables.sql"))  {$CT_file = 1}
if (open (CREATE_INDEXES_IN,"<".$orig_filename_root.".create-indexes.sql")) {$CI_file = 1}
if (open (CREATE_FKEYS_IN,  "<".$orig_filename_root.".create-fkeys.sql"))   {$CF_file = 1}
if (open (DROP_TABLES_IN,   "<".$orig_filename_root.".drop-tables.sql"))    {$DT_file = 1}
if (open (DROP_INDEXES_IN,  "<".$orig_filename_root.".drop-indexes.sql"))   {$DI_file = 1}
if (open (DROP_FKEYS_IN,    "<".$orig_filename_root.".drop-fkeys.sql"))     {$DF_file = 1}

print "CT_file:         ",$CT_file,"\n";
print "CI_file:         ",$CI_file,"\n";
print "CF_file:         ",$CF_file,"\n";
print "DT_file:         ",$DT_file,"\n";
print "DI_file:         ",$DI_file,"\n";
print "DF_file:         ",$DF_file,"\n";

# Open the 6 output files
if ($CT_file) { open (CREATE_TABLES_OUT, ">".$new_filename_root.".create-tables.sql") };
if ($CI_file) { open (CREATE_INDEXES_OUT,">".$new_filename_root.".create-indexes.sql") };
if ($CF_file) { open (CREATE_FKEYS_OUT,  ">".$new_filename_root.".create-fkeys.sql") };
if ($DT_file) { open (DROP_TABLES_OUT, ">".$new_filename_root.".drop-tables.sql") };
if ($DI_file) { open (DROP_INDEXES_OUT,">".$new_filename_root.".drop-indexes.sql") };
if ($DF_file) { open (DROP_FKEYS_OUT,  ">".$new_filename_root.".drop-fkeys.sql") };

# Copy all lines from each input file to each output file
# changing the given orig_prefix to the new_prefix

if ($CT_file) { $tmp = CHANGE_PREFIX(\*CREATE_TABLES_IN,\*CREATE_TABLES_OUT,$orig_prefix,$new_prefix) }
if ($CI_file) { $tmp = CHANGE_PREFIX(\*CREATE_INDEXES_IN,\*CREATE_INDEXES_OUT,$orig_prefix,$new_prefix) }
if ($CF_file) { $tmp = CHANGE_PREFIX(\*CREATE_FKEYS_IN,\*CREATE_FKEYS_OUT,$orig_prefix,$new_prefix) }
if ($DT_file) { $tmp = CHANGE_PREFIX(\*DROP_TABLES_IN,\*DROP_TABLES_OUT,$orig_prefix,$new_prefix) }
if ($DI_file) { $tmp = CHANGE_PREFIX(\*DROP_INDEXES_IN,\*DROP_INDEXES_OUT,$orig_prefix,$new_prefix) }
if ($DF_file) { $tmp = CHANGE_PREFIX(\*DROP_FKEYS_IN,\*DROP_FKEYS_OUT,$orig_prefix,$new_prefix) }

# Close all input files:
close (CREATE_TABLES_IN);
close (CREATE_INDEXES_IN);
close (CREATE_FKEYS_IN);
close (DROP_TABLES_IN);
close (DROP_INDEXES_IN);
close (DROP_FKEYS_IN);

# Close all output files:
close (CREATE_TABLES_OUT);
close (CREATE_INDEXES_OUT);
close (CREATE_FKEYS_OUT);
close (DROP_TABLES_OUT);
close (DROP_INDEXES_OUT);
close (DROP_FKEYS_OUT);

exit();

# CHANGE_PREFIX(FILE_IN,FILE_OUT,$orig_prefix,$new_prefix);
sub CHANGE_PREFIX {
#    my*...) = @_;
	local($file_in)=@_[0];
	local($file_out)=@_[1];
	local($orig_prefix)=@_[2];
	local($new_prefix)=@_[3];

	while (<$file_in>) {
		$input_line = $_ ;
		$output_line = $input_line;
		$output_line =~ s/$orig_prefix/$new_prefix/ge;
		printf ($file_out $output_line);
	}
	return;
}
