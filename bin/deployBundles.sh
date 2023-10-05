#!/bin/sh
#
# Just reuse from tcp-22.

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

validateHOSTDIRFG

cd $PROJECT_HOME/fg-raw-data || checkrc

export HOSTDIR=$HOSTDIRFG

sh $TCP22DIR/bin/deployBundle.sh sgmaterial || checkrc
