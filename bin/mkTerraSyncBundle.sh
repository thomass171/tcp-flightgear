#!/bin/sh
#
# Bundle TerraSync data in the 2018 style. Reads all files in $TERRASYNCDIR (if no option is set)
# and copies/converts the files.
# Per Option an alternatives dir can be set for bundling Custom Sceneries.
#  - just copies (no rsync Probleme/Loeschungen)
#  - es wird nicht mehr versucht, nur die in stg verwendeten Model zu bundeln, einfach alles
#  - der Aufbau/die Struktur der Bundle bleibt aber erhalten. Die Terrain und Objects STGs
#  - kommen ins selbe Bundle.
#
# Option -f for overwriting (for EDDK)
#	
#

OWNDIR=`dirname $0`
TCP22DIR=$OWNDIR/../../tcp-22
source $TCP22DIR/bin/common.sh || exit 1
source $OWNDIR/common.sh || exit 1

validateHOSTDIRFG

usage() {
	echo "usage: $0 [-f] [<terrasyncdir>]"
	exit 1
}

#
# Convert or copy a single TerraSync File to bundle.
# The file type (suffix) decides how to handle the file.
#
processTerraSyncFile() {
	filename=$1
	if [ -d $filename ]
	then
	  # create that directory in bundles
		verboselog mkdir -p $TERRASYNCBUNDLEDIR/$filename
		mkdir -p $TERRASYNCBUNDLEDIR/$filename
	else
		DIRNAME=`dirname $filename`
		BASENAME=`basename $filename`
		#SUFFIX=`echo $filename|awk -F"." '{print $(NF)} '`
		SUFFIX="${filename##*.}"
		if [ "$SUFFIX" = "gz" ]
		then
			SUFFIX=btg.gz
		fi
		DESTDIR=$TERRASYNCBUNDLEDIR/$DIRNAME
		BASENAME=`basename $BASENAME .$SUFFIX`
		export DIRNAME SUFFIX DESTDIR BASENAME
		echo processing $filename":" $BASENAME $SUFFIX
		case $SUFFIX in
			# maybe 'rgb' should be discarded. Who can read it at all?
			"xml"|"png"|"jpg"|"stg"|"rgb")
				if [ ! -r $DESTDIR/$BASENAME.$SUFFIX -o $FORCE = "1" ]
				then
					#echo cp file
					cp -p $filename $DESTDIR
				fi
				;;
			"ac")
				if [ ! -r $DESTDIR/$BASENAME.gltf -o $FORCE = "1" ]
				then
					preprocessGLTF $filename $DESTDIR
					relax
				fi
				;;
			"btg")
				if [ ! -r $DESTDIR/$BASENAME.gltf -o $FORCE = "1" ]
				then
					preprocessGLTF $filename $DESTDIR
					relax
				fi
				;;
			"btg.gz")
				if [ ! -r $DESTDIR/$BASENAME.gltf -o $FORCE = "1" ]
				then
					TMPFILE=$DESTDIR/$BASENAME-tmp.btg
					cat $filename | gunzip > $TMPFILE
					preprocessGLTF $TMPFILE $DESTDIR
					checkrc preprocessGLTF
					rm -f $TMPFILE
					relax
				fi
				;;
			*)
				echo "unknown suffix $SUFFIX"
		esac
	fi
}

#
# is it really a valid terrasync tree?
#
validateTERRASYNCDIR() {
  [ ! -d $TERRASYNCDIR/Objects ] && error "No Objects subdir in $TERRASYNCDIR"
  [ ! -d $TERRASYNCDIR/Terrain ] && error "No Terrain subdir in $TERRASYNCDIR"
  [ ! -d $TERRASYNCDIR/Models ] && error "No Models subdir in $TERRASYNCDIR"
}

export TERRASYNCBUNDLEDIR=$HOSTDIRFG/bundles/TerraSync

mkdir -p $TERRASYNCBUNDLEDIR
checkrc mkdir

FORCE=0
if [ "$1" = "-f" ]
then
	FORCE=1
	shift
fi

TERRASYNCDIR=$OWNDIR/../fg-raw-data/terrasync
if [ ! -z "$1" ]
then
	TERRASYNCDIR=$1
	shift
fi
validateTERRASYNCDIR
cd $TERRASYNCDIR && checkrc
echo "Processing files in:" `pwd`
echo "CLASSPATH="$CLASSPATH

# process files and directories
find Models Objects Terrain | egrep -v ".svn|.dirindex|.hashes|.DS_Store" | while read filename
do
	processTerraSyncFile $filename
done

echo "Completed. Building model directory"

#jetzt das model directory. Frueher waren da auch model aus Objects drin. Jetzt erstmal nicht mehr
#19.5.18: Was passiert hier jetzt? Die Dateien sind jetzt alle in die Bundleverzeichnisse
#kopiert. Jetzt werden noch die drietory.txt erstellt, erst der generischen model und
#dann pro stg.
MODELDIRECTORY=$TERRASYNCBUNDLEDIR/directory-model.txt
cd $TERRASYNCBUNDLEDIR && checkrc
find Models -type f  > $MODELDIRECTORY

echo "Completed. Building not existing STG directories"

#und die stg directories
cd $TERRASYNCBUNDLEDIR && checkrc
find . -name "*.stg" | awk -F"/" '{print $5}' | awk -F"." '{print $1}' | while read filename
do
	if [ ! -r directory-$filename.txt ]
	then
		echo STG:$filename
		$GRANADADIR/bin/mkStgBundle.sh $filename
	fi
done
echo "Completed."
exit 0

