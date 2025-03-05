#!/bin/sh
#
# Just reuse from tcp-22.

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

validateHOSTDIRFG

export HOSTDIR=$HOSTDIRFG

# 25.2.25 copy solution for bundle engine and data (needed for wood textures in railing)
# no longer needed

cd $PROJECT_HOME || checkrc
sh $TCP22DIR/bin/deployBundle.sh -m traffic-fg || checkrc
sh $TCP22DIR/bin/deployBundle.sh -m traffic-advanced || checkrc

cd $PROJECT_HOME/fg-raw-data || checkrc
sh $TCP22DIR/bin/deployBundle.sh fgdatabasic || checkrc
sh $TCP22DIR/bin/deployBundle.sh sgmaterial || checkrc
sh $TCP22DIR/bin/deployBundle.sh bluebird || checkrc