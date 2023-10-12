#!/bin/bash
#
# Deploy the GWT/webgl build artifacts from $BUILDDIR to $HOSTDIRFG (which might be a remote directory)
# Also deploys the HTML landing page (tcp-flightgear.html)? Or reuse/include to  tcp??
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

validateHOSTDIRFG

# Default Values. Default host is localhost, a local installation just copying.

export HOSTDIR=$HOSTDIRFG
export BUILDDIR=platform-webgl-ext/target/module-platform-webgl-ext-1.0.0-SNAPSHOT
sh $TCP22DIR/bin/deploy.sh -b $BUILDDIR || checkrc
