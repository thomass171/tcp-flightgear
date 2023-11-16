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

# temporary soltution for bundle engine
cp -rp $HOSTDIRFG/../tcp-22/bundles/engine $HOSTDIRFG/bundles

cd $PROJECT_HOME || checkrc
sh $TCP22DIR/bin/deployBundle.sh -m traffic-fg || checkrc

cd $PROJECT_HOME/fg-raw-data || checkrc
sh $TCP22DIR/bin/deployBundle.sh fgdatabasic || checkrc