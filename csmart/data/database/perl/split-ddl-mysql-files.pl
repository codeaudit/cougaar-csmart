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

# <copyright>
#  Copyright 2003 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
# 
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the Cougaar Open Source License as published by
#  DARPA on the Cougaar Open Source Website (www.cougaar.org).
# 
#  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
#  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
#  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
#  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
#  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
#  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
#  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
#  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
#  PERFORMANCE OF THE COUGAAR SOFTWARE.
# </copyright>


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
	$input_line = FIX_MYSQL_COMMENTS($input_line);
    if ($sql_mode eq $CTab) { # Create Table Mode
        if(substr($input_line,0,9) eq "## INDEX:") {
			$sql_mode = $CInd;
			print "sql_mode: ",$sql_mode,"\n";
		}
		else {
			$input_line_2 = FIX_PK_INPUT_LINE($input_line,$index_prefix);
			$input_line_3 = FIX_MYSQL_DATA_TYPE($input_line_2);
			$input_line_4 = FIX_MYSQL_TABLE_TYPE($input_line_3);
			printf (CREATE_TABLES_OUT $input_line_4)
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

# Open the 2 CREATE files for reading to make the 2 DROP files
open (CREATE_TABLES_IN,  $filename_root.".create-mysql-tables.sql");
open (CREATE_INDEXES_IN, $filename_root.".create-mysql-indexes.sql");
#open (CREATE_FKEYS_IN,   $filename_root.".create-fkeys.sql");
# Open the 2 DROP output files
open (DROP_TABLES_OUT,   ">".$filename_root.".drop-mysql-tables.sql");
open (DROP_INDEXES_OUT,  ">".$filename_root.".drop-mysql-indexes.sql");
#open (DROP_FKEYS_OUT,    ">".$filename_root.".drop-fkeys.sql");

# Create DROP_TABLES
while (<CREATE_TABLES_IN>) {
    $input_line = $_ ;
	if (substr($input_line,0,12) eq "CREATE TABLE") {
		$length = length($input_line);
		$table_name = substr($input_line,13,$length-15);
		printf (DROP_TABLES_OUT "DROP TABLE IF EXISTS ".$table_name.";\n");
	}
}
# Create DROP_INDEXES
while (<CREATE_INDEXES_IN>) {
	$input_line = $_ ;
	if (substr($input_line,0,12) eq "CREATE INDEX") {
		$output_line = $input_line;
		$output_line =~ s/CREATE /DROP /;
		$output_line =~ s/\(.*\)//;
		printf (DROP_INDEXES_OUT $output_line);

#		$length = length($input_line);
#		$remainder = substr($input_line,13,$length-15);
#        ($index_name, $etc) = split(' ',$remainder,2);
#		printf (DROP_INDEXES_OUT "DROP INDEX ".$index_name.";\n");
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

# Open the CREATE TABLE file for reading to make the LOAD DATA file
open (CREATE_TABLES_IN,  $filename_root.".create-mysql-tables.sql");
# Open the LOAD DATA file
open (LOAD_TABLES_OUT,   ">".$filename_root.".load-mysql-tables.sql");

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
			if ($input_line_2 =~ m/UNIQUE KEY/ || $input_line_2 =~ m/TYPE=MyISAM/ ) {
				last;
			}
			else {
				($column_name,$remainder) = split(' ',$input_line_2,2);  # The column name is first
				$output_text = $output_text.$column_name.",";            #Append the column name
#				print ($column_name,":: ",$input_line_2,"\n") # Debug printout
			}
		}
		$output_text = $output_text.");\n";                   # Tack on a ");"
		$output_text =~ s/,\)/\)/;                            # Remove the last ","
		printf (LOAD_TABLES_OUT $output_text."\n");           # Dump the $output_text
	}
}

# Close all the files
close (CREATE_TABLES_IN);
close (LOAD_TABLES_OUT);

exit();

# FORM_LOAD_DATA_TABLE(table_name);
sub FORM_LOAD_DATA_TABLE {
	$infile_prefix = ":cip/csmart/data/database/csv/";
    local($table_name)=@_[0];

	$output_text =              "LOAD DATA INFILE '".$infile_prefix.$table_name.".csv.tmp'\n";
	$output_text = $output_text."    INTO TABLE ".$table_name."\n";
	$output_text = $output_text."    FIELDS\n";
	$output_text = $output_text."        TERMINATED BY ','\n";
	$output_text = $output_text."        OPTIONALLY ENCLOSED BY '\"'\n";
	$output_text = $output_text."    LINES TERMINATED BY '\\n'\n";
	$output_text = $output_text."    IGNORE 1 LINES\n";
	$output_text = $output_text."    (";
	return $output_text;
}


# FIX_MYSQL_COMMENT_CHAR(input_line)
sub FIX_MYSQL_COMMENTS {
	local($input_line)=@_[0];

	$input_line_1 = $input_line;
	if ($input_line_1 =~ m/--/) {
	    $input_line_1 =~ s/--/##/;                         # Replace -- with ## as comment character
	}
	if ($input_line_1 =~ m/^COMMENT ON /) {
		$input_line_1 =~ s/^COMMENT ON /## COMMENT ON /;   # Replace COMMENT with ##COMMENT
		while (<ALL_DDL_IN>) {                             # Read the next line
			$input_line_2 = $_ ;                           # Ignore it. It should be just a ;
			last;
		}
		$input_line_1 = $input_line_1."\n";                # Stick a newline on the end of input_line_1
#		print "input_line:   ",$input_line,"\n";
#		print "input_line_1: ",$input_line_1,"\n";
	}
	return $input_line_1;
}



# FIX_PK_INPUT_LINE(input_line,index_prefix)
sub FIX_PK_INPUT_LINE {
	local($input_line)=@_[0];
	local($index_prefix)=@_[1];

	if (substr($input_line,0,18) eq "    CONSTRAINT pk_") {
		$length = length($input_line);
		$remainder = substr($input_line,18,$length-18);
		($table_name, $etc) = split(' ',$remainder,2);
		$input_line_1 = "    CONSTRAINT pk_".$index_prefix.$table_name." ".$etc;
	    $input_line_1 =~ s/PRIMARY KEY //;
	    $input_line_1 =~ s/CONSTRAINT /UNIQUE KEY /;
#		print "input_line:   ",$input_line,"\n";
#		print "input_line_1: ",$input_line_1,"\n";
	}
	else {
		$input_line_1 = $input_line;
	}
	return $input_line_1;
}

# FIX_MYSQL_TABLE_TYPE(input_line)
sub FIX_MYSQL_TABLE_TYPE {
	local($input_line)=@_[0];

	if(substr($input_line,0,2) eq ") ") {
		$input_line_1 = $input_line;
		$input_line_1 =~ s/\)/\) TYPE=MyISAM/;
#		print "input_line:   ",$input_line,"\n";
#		print "input_line_1: ",$input_line_1,"\n";
	}
	else {
		$input_line_1 = $input_line;
	}
	return $input_line_1;
}


# FIX_MYSQL_DATA_TYPE(input_line)
sub FIX_MYSQL_DATA_TYPE {
	local($input_line)=@_[0];

# convert from oracle datatypes to mysql

    ############################################
    # Rules are..:
    # Datatype (ORA)   Length  Returns (MySQL)
    # NUMBER           Any     DECIMAL
    # DEC              Any     DECIMAL
    # DECIMAL          Any     DECIMAL
    # NUMERIC          Any     DECIMAL
    # DOUBLE PRECISION Any     DECIMAL
    # FLOAT            Any     DECIMAL
    # REAL             Any     DECIMAL
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
	    $input_line =~ s/NUMBER/DECIMAL/;
		if ($input_line =~ m/NOT NULL/) {
			$input_line =~ s/NOT NULL/NOT NULL DEFAULT '0.000000000000000000000000000000',/;
		}
		else {
			$input_line =~ s/\)/\) default NULL/;
		}
	    $input_line_1 = $input_line;
	}
	elsif ($input_line =~ m/NUMBER /) {
	    $input_line =~ s/NUMBER /DECIMAL(68,30)/;
		if ($input_line =~ m/NOT NULL/) {
			$input_line =~ s/NOT NULL/NOT NULL DEFAULT '0.000000000000000000000000000000'/;
		}
		else {
			$input_line =~ s/\)/\) DEFAULT NULL/;
		}
	    $input_line_1 = $input_line;
	}
	elsif ($input_line =~ m/NUMBER,/) {
	    $input_line =~ s/NUMBER,/DECIMAL(68,30),/;
		if ($input_line =~ m/NOT NULL/) {
			$input_line =~ s/NOT NULL/NOT NULL DEFAULT '0.000000000000000000000000000000'/;
		}
		else {
			$input_line =~ s/\)/\) DEFAULT NULL/;
		}
	    $input_line_1 = $input_line;
	}
	elsif ($input_line =~ m/ DATE /) {
	    $input_line =~ s/ DATE / DATETIME /;
		if ($input_line =~ m/NOT NULL/) {
			$input_line =~ s/NOT NULL/NOT NULL DEFAULT '0000-00-00 00:00:00'/;
		}
		else {
			$input_line =~ s/DATETIME/DATETIME             DEFAULT NULL/;
		}
	    $input_line_1 = $input_line;
	}
	elsif ($input_line =~ m/ DATE,/) {
	    $input_line =~ s/ DATE,/ DATETIME,/;
		if ($input_line =~ m/NOT NULL/) {
			$input_line =~ s/NOT NULL/NOT NULL DEFAULT '0000-00-00 00:00:00'/;
		}
		else {
			$input_line =~ s/DATETIME/DATETIME             DEFAULT NULL/;
		}
	    $input_line_1 = $input_line;
	}
	elsif ($input_line =~ m/VARCHAR2/) {
	    $input_line =~ s/VARCHAR2/VARCHAR/;
		if ($input_line =~ m/NOT NULL/) {
			$input_line =~ s/NOT NULL/BINARY NOT NULL DEFAULT ''/;
		}
		else {
			$input_line =~ s/\)/\)    BINARY DEFAULT NULL/;
		}
	    $input_line_1 = $input_line;
	}
	elsif ($input_line =~ m/ CHAR\(/) {
		if ($input_line =~ m/NOT NULL/) {
			$input_line =~ s/NOT NULL/BINARY NOT NULL DEFAULT ''/;
		}
		else {
			$input_line =~ s/\)/\)       BINARY DEFAULT NULL/;
		}
	    $input_line_1 = $input_line;
	}
	else {
		$input_line_1 = $input_line;
	}
	return $input_line_1;
}
