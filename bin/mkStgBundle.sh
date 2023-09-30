#!/bin/sh
#
# Create a directory txt file for a single STG by its number (Bucketindex?).
# Eg. 3056435.stg will have directory-3056435.txt
# Direectories always reside in top dir (See '2018 layout style').
# The directory will list terrain and object files of the STG. 'ac' filenames will keep suffix 'ac' even though
# the models were converted to 'gltf'. The loader is aware of that.
#
#	Erstellt ein Bundle fuer ein einzelnes STG File, bzw. alle zu einer Nummer.
#	1.1.18: Deprecated zugunsten von mkTerraSyncBundle.sh.
#	Kann standalone oder embedded aufgerufen werden, muss aber im gewuenschten
#	Verzeichnis stehen.
#	erstellt aber kein modeldirectory mehr.
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

extractacfile() {
	grep "\\.ac" $TERRASYNCDIR/$1 | awk -F'>' '{print $2}' | awk -F'<' '{print $1}'
	checkrc grep
}
 
# $1=dir
# $2=modelfile
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
# Fuer ein einzelnes Modelfile: Wenn xml, um ac. ergaenzen, wenn ac, um Texturen ergaenzen
# $1=modelfile
# $2=directory
# Erst hier erfolgt fuers directory die Umbenennung nach gltf
#
processSingleModelfile() {
	FILE=$1
	#echo $FILE 
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

# make sure we are in a terrasync directory
if [ ! -d Terrain -o ! -d Objects -o ! -d Models ]
then
	error "not in TerraSync dir"
fi

CURRENTDIR=`pwd`
DIRECTORY=$CURRENTDIR/directory-$STGNO.txt
export DIRECTORY TERRASYNCDIR
rm -f $DIRECTORY

find . -name $STGFILE | awk '{print substr($1,3)}' | while read filename
do
	#echo $filename
	DIRNAME=`dirname $filename`
	export DIRNAME
	#echo "dirname=$DIRNAME"
	echo $filename >> $DIRECTORY
	#cd $TERRASYNCDIR/$DIRNAME && checkrc
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

exit 0

