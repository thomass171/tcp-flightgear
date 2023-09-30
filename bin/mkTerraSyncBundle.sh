#!/bin/sh
#
# Bundle TerraSync data in the '2018 layout style'. Reads all files in $TERRASYNCDIR (if no option is set)
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
					mv $DESTDIR/$BASENAME-tmp.gltf $DESTDIR/$BASENAME.gltf
					mv $DESTDIR/$BASENAME-tmp.bin $DESTDIR/$BASENAME.bin
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
echo "CLASSPATH="$CLASSPATH
echo "Ready to process files in:" `pwd` "to $TERRASYNCBUNDLEDIR. Hit CR"
read

# process files and directories
find Models Objects Terrain | egrep -v ".svn|.dirindex|.hashes|.DS_Store" | while read filename
do
	processTerraSyncFile $filename
	# exit via checkrc only terminates the while loop!
	checkrc processTerraSyncFile
done
if [ $? != 0 ]
then
    # already reported
    exit 1
fi



#jetzt das model directory. Frueher waren da auch model aus Objects drin. Jetzt erstmal nicht mehr
#19.5.18: Was passiert hier jetzt? Die Dateien sind jetzt alle in die Bundleverzeichnisse
#kopiert.
# Now create directory files ('directory[-no].txt'), first for generic model ...
echo "Completed. Building generic model directory..."
MODELDIRECTORY=$TERRASYNCBUNDLEDIR/directory-model.txt
cd $TERRASYNCBUNDLEDIR && checkrc
find Models -type f  > $MODELDIRECTORY
echo "... completed."

echo "Building not existing STG directories..."
# ... and now build directory for each stg
cd $TERRASYNCBUNDLEDIR && checkrc
find . -name "*.stg" | awk -F"/" '{print $5}' | awk -F"." '{print $1}' | while read filename
do
	if [ ! -r directory-$filename.txt ]
	then
		echo STG:$filename
		sh $PROJECT_HOME/bin/mkStgBundle.sh $filename || exit 1
	fi
done
if [ $? != 0 ]
then
    # already reported
    exit 1
fi
echo "... completed."
exit 0

