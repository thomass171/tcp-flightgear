#!/bin/bash
#
#
# Convert a 'ac' or 'btg' file to GLTF
# $1=inputfile
# $2=destination directory
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1


export HOSTDIR=$HOSTDIRFG
export BUILDDIR=platform-webgl-ext/target/module-platform-webgl-ext-1.0.0-SNAPSHOT
#sh $TCP22DIR/bin/deploy.sh -b $BUILDDIR || checkrc

echo "Converting $1 in" `pwd` "to GLTF in" $2
#18.4.17 leaves destdir!cd $GRANADADIR/desktop
# needs a 'sgmaterial' bundle
export ADDITIONALBUNDLE=$HOSTDIRFG/bundles
java -Djava.awt.headless=true de.yard.threed.tools.GltfProcessor -gltf -o $2 "$1" -l de.yard.threed.toolsfg.LoaderBTGBuilder
exit $?

