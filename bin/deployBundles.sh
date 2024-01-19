#!/bin/sh
#
# Just reuse from tcp-22.

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

validateHOSTDIRFG

cd $PROJECT_HOME/fg-raw-data || checkrc

export HOSTDIR=$HOSTDIRFG

sh $TCP22DIR/bin/deployBundle.sh sgmaterial || checkrc

sh $TCP22DIR/bin/deployBundle.sh bluebird || checkrc

# temporary solution for bundle engine
cp -rp $HOSTDIRFG/../tcp-22/bundles/engine $HOSTDIRFG/bundles
# temporary solution for bundle data. Needed for wood textures in railing
cp -rp $HOSTDIRFG/../tcp-22/bundles/data $HOSTDIRFG/bundles

cd $PROJECT_HOME || checkrc
sh $TCP22DIR/bin/deployBundle.sh -m traffic-fg || checkrc
sh $TCP22DIR/bin/deployBundle.sh -m traffic-advanced || checkrc

cd $PROJECT_HOME/fg-raw-data || checkrc
sh $TCP22DIR/bin/deployBundle.sh fgdatabasic || checkrc