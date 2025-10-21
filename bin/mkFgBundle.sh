#!/bin/sh
#
# Wrapper of deployBundle.sh for convenient converting of a FG parts.
#
# Reads all files from SOURCE definition in bundlename.bundledef
# and copies/converts the files to BUNDLEDIR (default overridden by option '-o').
#
# Should be executed in tcp-flightgear home directory (for correct set up of PROJECT_HOME
# needed for CLASSPATH and finding 'bundledefs').
#
# No need for an '-f' option. An existing destination file won't be (or shouldn't be) overwritten anyway.
#
# For converting a fgdatabasic tree according to the definition in bundledefs/fgdatabasic.bundledef the command
#   sh bin/mkFgBundle.sh -o /Volumes/Flightgear/hostdirfullfg/bundles fgdatabasic
# can be used. Or for sgmaterial
#   sh bin/mkFgBundle.sh -o /Volumes/Flightgear/hostdirfullfg/bundles sgmaterial
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

usage() {
	echo "usage: $0 [-v] [-o bundledir] <bundlename>"
	exit 1
}

VERBOSE=
if [ "$1" = "-v" ]
then
	export VERBOSE=-v
	shift
fi
if [ "$1" = "-o" ]
then
  shift
	export BUNDLEDIR=$1
	shift
fi
export BUNDLENAME=$1

# BUNDLEDIR will default to $HOSTDIR/bundles
if [ ! -z "$BUNDLEDIR" ]
then
  if [ ! -d $BUNDLEDIR ]
  then
    error $BUNDLEDIR not found
  fi
fi

# Reuse existing bundle building/deploy script
sh $TCP22DIR/bin/deployBundle.sh $VERBOSE $BUNDLENAME
checkrc deployBundle

echo "... completed."
exit 0