#!/bin/sh
#
# Wrapper of deployBundle.sh for convenient converting of a FG aircraft tree.

# Reads all files from
#   $AIRCRAFTDIR (without default)
# and copies/converts the files to
#   $AIRCRAFTBUNDLEDIR(without default).
#
# Should be executed in tcp-flightgear home directory (for correct set up of PROJECT_HOME
# needed for CLASSPATH and finding 'bundledefs').
#
# No need for an '-f' option. An existing destination file won't be (or shouldn't be) overwritten anyway.
#
# For converting a c172p tree according to the definition in bundledefs/c172p.bundledef the command
#   sh bin/mkAircraftBundle.sh -o /Volumes/Flightgear/bundletarget c172
# can be used.
# 25.9.25 TODO merge with mkFgBundle.sh

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

usage() {
	echo "usage: $0 [-v] [-o aircraftbundledir] <aircraftname>"
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
	export AIRCRAFTBUNDLEDIR=$1
	shift
fi
export AIRCRAFTBASENAME=$1

# BUNDLEDIR will default to $HOSTDIR/bundles
if [ ! -z "$AIRCRAFTBUNDLEDIR" ]
then
  if [ ! -d $AIRCRAFTBUNDLEDIR ]
  then
    error $AIRCRAFTBUNDLEDIR not found
  fi
  export BUNDLEDIR=$AIRCRAFTBUNDLEDIR
fi

# Reuse existing bundle building/deploy script
sh $TCP22DIR/bin/deployBundle.sh $VERBOSE $AIRCRAFTBASENAME
checkrc deployBundle

echo "... completed."
exit 0