#!/bin/sh
#
# Create a directory txt file for a single STG by its number (Bucketindex?).
# Eg. 3056435.stg will have directory-3056435.txt.
#
# Needs to be executed in the destination directory and needs to know the source directory (TERRASYNCDIR).
#
# The name mkStgBundle.sh is confusing (it once was for bundle building)
# Directories always reside in top dir (See '2018 layout style').
# The directory will list terrain and object files of the STG. 'ac' filenames will keep suffix 'ac' even though
# the models were converted to 'gltf'. The loader is aware of that.
#
#	Not for creating 'Model' directory (directory-model.txt).
#	Der processStaticObject ist erforderlich um zu ermitteln, zu welchem Bundle denn die
#	in Objects liegenden Model Dateien gehoeren. Da kommen ja mehrere STGs/Bundles in Frage.
#

OWNDIR=`dirname $0`

source $OWNDIR/common.sh || exit 1
#set -x

usage() {
	echo "$0: <stgnumber>"
	exit 1
}

#
# Extract an ac filename that is referenced in an XML file
#
extractacfile() {
	grep "\\.ac" $TERRASYNCDIR/$1 | awk -F'>' '{print $2}' | awk -F'<' '{print $1}'
	checkrc grep
}

# Extract texture names from an 'ac' file for adding to the directory.
# $1=dir
# $2='ac' modelfile
# $3=directory
extracttexturefiles() {
	DP1=$1
	export DP1
	grep "texture" $TERRASYNCDIR/$2 | awk -F'"' '{print ENVIRON["DP1"]"/"$2}'  >> $3
	checkrc grep
}
 
#DIRNAME is exported
processStaticObject() {
	ODIR=` echo $DIRNAME | awk '{print substr($1,9)}' ` 
	while read filename
	do
		MODELFILE=Objects/$ODIR/$filename
		#echo $MODELFILE
		processSingleModelfile $MODELFILE $DIRECTORY
	done 
}

#
# Process a single model file: if xml, add ac files, if ac, add textures. The files are taken from the
# STG file, so if a XML is listed, the STG won't list the 'ac' file additionally.
# For XML this method is called recursively for handling the corresponding 'ac'.
# $1=modelfile
# $2=directory file
# Only here is the renaming from 'ac' to 'gltf' for the directory.
#
#
processSingleModelfile() {
	FILE=$1
	[ "$VERBOSE" = 1 ] && echo processSingleModelfile: $FILE
	# Adding gltfs here might lead to duplicates, but these are removed later
	echo $FILE | sed 's/.ac$/.gltf/' >> $2
	echo $FILE | sed 's/.ac$/.bin/' >> $2
	fileext=${FILE##*.}
	DIR=`dirname $FILE`
	case  $fileext in
		"xml")
			acfile=`extractacfile $FILE` 
			#echo "acfile" $DIR $acfile
			processSingleModelfile $DIR/$acfile $2
			;;
		"ac")
			extracttexturefiles $DIR $FILE $2
			;;
	esac
}

NOPP=0
if [ "$1" = "--nopp" ]
then
	NOPP=1
	shift
fi

if [ -z "$1" ]
then
	usage
fi

STGNO=$1
STGFILE=$STGNO.stg
shift

# make sure we are in a terrasync directory. Should
# be in the destination directory where directory...txt is to be created.
if [ ! -d Terrain -o ! -d Objects -o ! -d Models ]
then
	error "not in TerraSync dir"
fi
if [ -z "$TERRASYNCDIR" ]
then
  error "TERRASYNCDIR not set"
fi

CURRENTDIR=`pwd`
DIRECTORY=$CURRENTDIR/directory-$STGNO.txt
export DIRECTORY TERRASYNCDIR
rm -f $DIRECTORY

# process all STG files with that name. There might be multiple
find . -name $STGFILE | awk '{print substr($1,3)}' | while read filename
do
	[ "$VERBOSE" = 1 ] && echo "found STG " $filename
	DIRNAME=`dirname $filename`
	export DIRNAME
	#echo "dirname=$DIRNAME"
	echo $filename >> $DIRECTORY

	# "ac" und "btg" Eintraege werden im directory durch gltf ersetzt
	# der bin Eintrag fuer gltf kommt noch so dazu
	awk '{
		if ($1 == "OBJECT_BASE") {
			BASEFILE=substr($2,0,length($2)-4);
			print ENVIRON["DIRNAME"] "/" BASEFILE ".gltf";
			print ENVIRON["DIRNAME"] "/" BASEFILE ".bin";
		}
		if ($1 == "OBJECT") {
			BASEFILE=substr($2,0,length($2)-4);
			print ENVIRON["DIRNAME"] "/" BASEFILE ".gltf";
			print ENVIRON["DIRNAME"] "/" BASEFILE ".bin";
		}
	}' $filename >> $DIRECTORY
	awk '{
		if ($1 == "OBJECT_STATIC") {
			print $2 ;
		}
	}' $filename | processStaticObject  
done

sort -u $DIRECTORY -o $DIRECTORY

cat $DIRECTORY
chmod 755 $DIRECTORY

exit 0

