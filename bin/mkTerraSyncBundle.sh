#!/bin/sh
#
# Bundle TerraSync data in the '2018 layout style'. Reads all files from
#   $TERRASYNCDIR (default is $PROJECT_HOME/fg-raw-data/terrasync, might be overwritten by cmd arg)
# and copies/converts the files to
#   $TERRASYNCBUNDLEDIR(default is $HOSTDIRFG/bundles/TerraSync).
#
# Per Option an alternatives dir can be set for bundling Custom Sceneries.
#  - just copies (no rsync Probleme/Loeschungen)
#  - es wird nicht mehr versucht, nur die in stg verwendeten Model zu bundeln, einfach alles
#  - der Aufbau/die Struktur der Bundle bleibt aber erhalten.
#
# The Terrain and Objects STGs result in the same bundle (directory-no.txt).
#
# Option -f for force overwriting existing destination files
# Option -v for verbose
#
# For converting a TerraSync tree on an external disk the command
#   sh bin/mkTerraSyncBundle.sh -o /Volumes/Flightgear/TerraSync-Full /Volumes/Flightgear/TerraSync
# can be used. Be sure to use a full "sgmaterial" bundle instead of the subset. Conversion takes appx 8 hours for a 11GB TerraSync size.
#	
#

OWNDIR=`dirname $0`

source $OWNDIR/common.sh || exit 1

validateHOSTDIRFG

usage() {
	echo "usage: $0 [-f] [-v] [<terrasyncdir>]"
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
			# maybe 'rgb' should be discarded. Who can read it at all? (3.9.24 many base model use 'rgb'!)
			# 13.7.25 so we should convert it. BTW: LoaderAC renames references to 'rgb' in 'ac' files to 'png', so GLTF files will always use 'png'.
			"rgb")
        if [ ! -r $DESTDIR/$BASENAME.png -o $FORCE = "1" ]
        then
          # CmdLine tool of https://imagemagick.org. BTW, gimp knows how to display rgb files
          magick $filename $DESTDIR/$BASENAME.png
        fi
        ;;
      # 15.3.24: 'gltf' and 'bin' added
			"xml"|"png"|"jpg"|"stg"|"gltf"|"bin")
				if [ ! -r $DESTDIR/$BASENAME.$SUFFIX -o $FORCE = "1" ]
				then
					#echo cp file
					cp -p $filename $DESTDIR
				fi
				;;
			"ac")
				if [ ! -r $DESTDIR/$BASENAME.gltf -o $FORCE = "1" ]
				then
					sh $PROJECT_HOME/bin/convertModel.sh $filename $DESTDIR
					relax
				fi
				;;
			"btg")
				if [ ! -r $DESTDIR/$BASENAME.gltf -o $FORCE = "1" ]
				then
					sh $PROJECT_HOME/bin/convertModel.sh $filename $DESTDIR
					relax
				fi
				;;
			"btg.gz")
				if [ ! -r $DESTDIR/$BASENAME.gltf -o $FORCE = "1" ]
				then
				  # 13.11.24 Using a modified file name for conversion leads to wrong 'bin' uri in the GLTF. So better
				  # keep filename but use /tmp
					TMPFILE=/tmp/$BASENAME.btg
					cat $filename | gunzip > $TMPFILE
					sh $PROJECT_HOME/bin/convertModel.sh $TMPFILE $DESTDIR
					checkrc $PROJECT_HOME/bin/convertModel.sh
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
# is $TERRASYNCDIR really a valid terrasync tree?
#
validateTERRASYNCDIR() {
  [ ! -d $TERRASYNCDIR/Objects ] && error "No Objects subdir in $TERRASYNCDIR"
  [ ! -d $TERRASYNCDIR/Terrain ] && error "No Terrain subdir in $TERRASYNCDIR"
  [ ! -d $TERRASYNCDIR/Models ] && error "No Models subdir in $TERRASYNCDIR"
}

export TERRASYNCBUNDLEDIR=$HOSTDIRFG/bundles/TerraSync



FORCE=0
if [ "$1" = "-f" ]
then
	FORCE=1
	shift
fi
if [ "$1" = "-v" ]
then
	export VERBOSE=1
	shift
fi
if [ "$1" = "-o" ]
then
  shift
	export TERRASYNCBUNDLEDIR=$1
	if [ ! -d $TERRASYNCBUNDLEDIR ]
	then
	    error $TERRASYNCBUNDLEDIR not found
	fi
	shift
fi

mkdir -p $TERRASYNCBUNDLEDIR
checkrc mkdir

# TERRASYNCDIR is the source for this conversion, which is the destination of terry sync.
export TERRASYNCDIR=$PROJECT_HOME/fg-raw-data/terrasync
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




# Now create directory files ('directory[-no].txt'), first for shared model ...
# 25.8.24: Even though we don't load it fully, we build TerraSync-model bundle directory (also files are still converted).
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
		echo "Building directory for STG" $filename from $TERRASYNCDIR
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

