# Splits the ddl sql file (foo.sql) created by er-studio into 3 pieces:
# foo.create-tables.sql, foo.create_indexes.sql, and foo.create-fkeys.sql
# Then creates 3 files for dropping these entities:
# foo.drop-tables.sql, foo.drop_indexes.sql, and foo.drop-fkeys.sql
#
# Adds a prefix to all index names, since er-studio does not do this
#
# Args
#   (0) Name of ddl sql file from er-studio
#   (1) Prefix string for index names

# Initialize the SQL Modes
$CTab = "create_tables";
$CInd = "create_indexes";
$CFk = "create_FK_constraints";
$sql_mode = $CTab;

# Get the arguments: 
$input_filename = @ARGV[0];   # input ddl sql file
$index_prefix   = @ARGV[1];   # prefix for index names

# Get filename root
$filename_root = $input_filename;
$filename_root =~ s/.sql//;
print "input_filename: ",$input_filename,"\n";
print "filename_root:  ",$filename_root,"\n";
print "index_prefix:   ",$index_prefix,"\n";

# Open the input file
open (ALL_DDL_IN,$input_filename);
# Open the 3 initial output files
open (CREATE_TABLES_OUT, ">".$filename_root.".create-tables.sql");
open (CREATE_INDEXES_OUT,">".$filename_root.".create-indexes.sql");
open (CREATE_FKEYS_OUT,  ">".$filename_root.".create-fkeys.sql");

# Copy all lines from ALL_DDL_IN to CREATE_TABLES_OUT, CREATE_INDEXES_OUT and CREATE_FKEYS_OUT
print "sql_mode: ",$sql_mode,"\n";
while (<ALL_DDL_IN>) {
	$input_line = $_ ;
    if ($sql_mode eq $CTab) { # Create Table Mode
        if(substr($input_line,0,9) eq "-- INDEX:") {
			$sql_mode = $CInd;
			print "sql_mode: ",$sql_mode,"\n";
		}
		else {
			$input_line_2 = FIX_PK_INPUT_LINE($input_line,$index_prefix);
			printf (CREATE_TABLES_OUT $input_line_2)
			}
    }
    if ($sql_mode eq $CInd) { # Create Index Mode
        if(substr($input_line,0,11) eq "ALTER TABLE") {
			$sql_mode = $CFk;
			print "sql_mode: ",$sql_mode,"\n";
		}
		else {
			if (substr($input_line,0,12) eq "CREATE INDEX") {
				$length = length($input_line);
				$remainder = substr($input_line,13,$length-14);
				($index_name, $etc) = split(' ',$remainder,2);
				$output_line = "CREATE INDEX ".$index_prefix.$index_name." ".$etc.";\n";
			}
			else {$output_line = $input_line}
			printf (CREATE_INDEXES_OUT $output_line)
			}
    }
    if ($sql_mode eq $CFk) { # Create Foreign Key Mode
		printf (CREATE_FKEYS_OUT $input_line);
    }
}
# Close all the open files
close (ALL_DDL_IN);
close (CREATE_TABLES_OUT);
close (CREATE_INDEXES_OUT);
close (CREATE_FKEYS_OUT);

# Open the 3 CREATE files for reading to make the 3 DROP files
open (CREATE_TABLES_IN,  $filename_root.".create-tables.sql");
open (CREATE_INDEXES_IN, $filename_root.".create-indexes.sql");
open (CREATE_FKEYS_IN,   $filename_root.".create-fkeys.sql");
# Open the 3 DROP output files
open (DROP_TABLES_OUT,   ">".$filename_root.".drop-tables.sql");
open (DROP_INDEXES_OUT,  ">".$filename_root.".drop-indexes.sql");
open (DROP_FKEYS_OUT,    ">".$filename_root.".drop-fkeys.sql");

# Create DROP_TABLES
while (<CREATE_TABLES_IN>) {
	$input_line = $_ ;
	if (substr($input_line,0,12) eq "CREATE TABLE") {
		$length = length($input_line);
		$table_name = substr($input_line,13,$length-15);
		printf (DROP_TABLES_OUT "DROP TABLE ".$table_name.";\n");
	}
}
while (<CREATE_INDEXES_IN>) {
	$input_line = $_ ;
	if (substr($input_line,0,12) eq "CREATE INDEX") {
		$length = length($input_line);
		$remainder = substr($input_line,13,$length-15);
        ($index_name, $etc) = split(' ',$remainder,2);
		printf (DROP_INDEXES_OUT "DROP INDEX ".$index_name.";\n");
	}
}
while (<CREATE_FKEYS_IN>) {
	$input_line = $_ ;
	if (substr($input_line,0,11) eq "ALTER TABLE") {
		$length = length($input_line);
		$output_line = substr($input_line,0,$length-2);
		$output_line =~ s/ ADD / DROP /;
		printf (DROP_FKEYS_OUT $output_line.";\n");
	}
}

# Close all the files
close (CREATE_TABLES_IN);
close (CREATE_INDEXES_IN);
close (CREATE_FKEYS_IN);
close (DROP_TABLES_OUT);
close (DROP_INDEXES_OUT);
close (DROP_FKEYS_OUT);

# Open the CREATE TABLE file for reading to make the LOAD DATA file
open (CREATE_TABLES_IN,  $filename_root.".create-tables.sql");
# Create the load-ctl sub-directory
unlink <./load-ctl/*>;                                        # Delete any existing files
rmdir "./load-ctl";                                           # Remove the directory
mkdir ("./load-ctl");                                         # Create the directory

# Create LOAD_DATA
while (<CREATE_TABLES_IN>) {
    $input_line = $_ ;
# Look for CREATE TABLE
	if (substr($input_line,0,12) eq "CREATE TABLE") {
		$length = length($input_line);
		$table_name = substr($input_line,13,$length-15);
		$output_text = FORM_LOAD_DATA_TABLE($table_name);     # Get most of the Load data text
		while (<CREATE_TABLES_IN>) {                          # Get the column names
			$input_line_2 = $_ ;
			if ($input_line_2 =~ m/CONSTRAINT pk_/ || m/^\)/) {
				last;
			}
			else {
				($column_name,$remainder) = split(' ',$input_line_2,2);  # The column name is first
				$output_text = $output_text."    ".$column_name;         #Append the column name
				$output_text = $output_text."        char terminated by ',' optionally enclosed by '\"',\n";
#				print ($column_name,":: ",$input_line_2,"\n") # Debug printout
			}
		}
		$output_text = $output_text.")\n";                   # Tack on a ")"
		$output_text =~ s/,\n\)/\n\)/;                        # Remove the last ","
        open (LOAD_TABLE_OUT,   ">"."./load-ctl/".$table_name.".ctl");
		printf (LOAD_TABLE_OUT $output_text."\n");           # Dump the $output_text
		close (LOAD_TABLE_OUT);
	}
}

# Close all the files
close (CREATE_TABLES_IN);

exit();

# FORM_LOAD_DATA_TABLE(table_name);
sub FORM_LOAD_DATA_TABLE {
    local($table_name)=@_[0];

	$output_text = "options (\n";
	$output_text = $output_text."  skip=1)\n";

	$output_text = $output_text."LOAD\n";
	$output_text = $output_text."    INTO TABLE ".$table_name."\n";

    $output_text = $output_text."    INSERT\n";
	$output_text = $output_text."    FIELDS\n";	
	$output_text = $output_text."    (\n";
	return $output_text;
}


# FIX_PK_INPUT_LINE(input_line,index_prefix)
sub FIX_PK_INPUT_LINE {
	local($input_line)=@_[0];
	local($index_prefix)=@_[1];

	if (substr($input_line,0,18) eq "    CONSTRAINT PK_") {
		$length = length($input_line);
		$remainder = substr($input_line,18,$length-18);
		($table_name, $etc) = split(' ',$remainder,2);
		$input_line_1 = "    CONSTRAINT PK_".$index_prefix.$table_name." ".$etc;
#		print "input_line:   ",$input_line,"\n";
#		print "input_line_1: ",$input_line_1,"\n";
	}
	else {
		$input_line_1 = $input_line;
	}

	return $input_line_1;
}
