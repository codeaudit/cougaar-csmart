# Splits the ddl sql file (foo.sql) created by er-studio into 2 pieces:
# foo.create-mysql-tables.sql and foo.create-mysql-indexes.sql
# Then creates 2 files for dropping these entities:
# foo.drop-mysql-tables.sql and foo.drop-mysql-indexes.sql
#
# Modified from original split-ddl-sql-files.pl
#
# Adds a prefix to all index names, since er-studio does not do this
#
# Args
#   (0) Name of ddl sql file from er-studio
#   (1) Prefix string for index names

# Initialize the SQL Modes
$CTab = "create_tables";
$CInd = "create_indexes";
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
open (CREATE_TABLES_OUT, ">".$filename_root.".create-mysql-tables.sql");
open (CREATE_INDEXES_OUT,">".$filename_root.".create-mysql-indexes.sql");

# Copy all lines from ALL_DDL_IN to CREATE_TABLES_OUT and CREATE_INDEXES_OUT
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
#    if ($sql_mode eq $CFk) { # Create Foreign Key Mode
#		printf (CREATE_FKEYS_OUT $input_line);
#    }
}
# Close all the open files
close (ALL_DDL_IN);
close (CREATE_TABLES_OUT);
close (CREATE_INDEXES_OUT);
#close (CREATE_FKEYS_OUT);

# Open the 3 CREATE files for reading to make the 3 DROP files
open (CREATE_TABLES_IN,  $filename_root.".create-mysql-tables.sql");
open (CREATE_INDEXES_IN, $filename_root.".create-mysql-indexes.sql");
#open (CREATE_FKEYS_IN,   $filename_root.".create-fkeys.sql");
# Open the 3 DROP output files
open (DROP_TABLES_OUT,   ">".$filename_root.".drop-tables.sql");
open (DROP_INDEXES_OUT,  ">".$filename_root.".drop-indexes.sql");
#open (DROP_FKEYS_OUT,    ">".$filename_root.".drop-fkeys.sql");

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
#while (<CREATE_FKEYS_IN>) {
#	$input_line = $_ ;
#	if (substr($input_line,0,11) eq "ALTER TABLE") {
#		$length = length($input_line);
#		$output_line = substr($input_line,0,$length-2);
#		$output_line =~ s/ ADD / DROP /;
#		printf (DROP_FKEYS_OUT $output_line.";\n");
#	}
#}


# Close all the open files
close (CREATE_TABLES_IN);
close (CREATE_INDEXES_IN);
#close (CREATE_FKEYS_IN);
close (DROP_TABLES_OUT);
close (DROP_INDEXES_OUT);
#close (DROP_FKEYS_OUT);


exit();


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
# convert from oracle datatypes to mysql

    ############################################
    # Rules are..:
    # Datatype (ORA)   Length  Returns (MySQL)
    # NUMBER           Any     NUMERIC
    # DEC              Any     NUMERIC
    # DECIMAL          Any     NUMERIC
    # NUMERIC          Any     NUMERIC
    # DOUBLE PRECISION Any     NUMERIC
    # FLOAT            Any     NUMERIC
    # REAL             Any     NUMERIC
    # SMALLINT         Any     SMALLINT
    # VARCHAR          <256    VARCHAR
    # VARCHAR2         <256    VARCHAR
    # CHAR             <256    CHAR
    # VARCHAR2         >255    VARCHAR  // mek
    # VARCHAR          >255    TEXT
    # CHAR             >255    TEXT
    # LONG             <256    VARCHAR
    # LONGRAW          <256    VARCHAR
    # RAW              <256    VARCHAR
    # LONG             >255    TEXT
    # LONGRAW          >255    TEXT
    # RAW              >255    TEXT
    # DATE             -       DATETIME (Since DATE in oracle can
    # include time information!)
    #################################################

	if ($input_line =~ m/NUMBER\(/) {
	    $input_line =~ s/NUMBER/NUMERIC/;
	    $input_line_1 = $input_line;
	}
	if ($input_line =~ m/NUMBER /) {
	    $input_line =~ s/NUMBER /NUMERIC(68,38)/;
	    $input_line_1 = $input_line;
	}
	if ($input_line =~ m/NUMBER,/) {
	    $input_line =~ s/NUMBER,/NUMERIC(68,38),/;
	    $input_line_1 = $input_line;
	}
	if ($input_line =~ m/ DATE /) {
	    $input_line =~ s/ DATE / DATETIME /;
	    $input_line_1 = $input_line;
	}
	if ($input_line =~ m/ DATE,/) {
	    $input_line =~ s/ DATE,/ DATETIME,/;
	    $input_line_1 = $input_line;
	}
	if ($input_line =~ m/VARCHAR2/) {
	    $input_line =~ s/VARCHAR2/VARCHAR/;
	    $input_line_1 = $input_line;
	}
	else {
		$input_line_1 = $input_line;
	}

	return $input_line_1;
}


