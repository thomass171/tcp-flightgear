#!/bin/bash
#
# Deploy the GWT/webgl build artifacts from $BUILDDIR to $HOSTDIRFG (which might be a remote directory)
# Also deploys the HTML landing page (tcp-flightgear.html)? Or reuse/include to  tcp??
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

validateHOSTDIRFG

# Default Values. Default host is localhost, a local installation just copying.

BUILDDIR=platform-webgl-fg/target/module-platform-webgl-fg-1.0.0-SNAPSHOT

usage() {
    echo "Usage: $0 [-b <builddir>] " 1>&2
    exit 1;
}

while getopts "b:s" o; do
    case "${o}" in
        b)
            BUILDDIR=${OPTARG}
            ;;
        s)
            COPYCMD="scp -pr"
            echo "Using scp. Be sure to delete deprecated files by hand."
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${BUILDDIR}" ]; then
    usage
fi

if [ ! -d $BUILDDIR ]; then
    error "no directory " $BUILDDIR
fi

echo Ready to deploy to $HOSTDIRFG. Hit CR
read

# rsync copy will delete deprecated files
#$COPYCMD $BUILDDIR/js $HOSTDIRFG
#$COPYCMD $BUILDDIR/webgl $HOSTDIRFG
#$COPYCMD $BUILDDIR/threejs $HOSTDIRFG
#$COPYCMD $BUILDDIR/webgl.html $HOSTDIRFG
#$COPYCMD $BUILDDIR/.htaccess $HOSTDIRFG
#$COPYCMD docs/tcp-22.html $HOSTDIRFG
#$COPYCMD docs/tcp-22.js $HOSTDIRFG
#$COPYCMD docs/util.js $HOSTDIRFG
#$COPYCMD docs/sceneutil.js $HOSTDIRFG
#$COPYCMD $BUILDDIR/version.html $HOSTDIRFG
#$COPYCMD docs/*.png $HOSTDIRFG
#$COPYCMD docs/favicon.ico $HOSTDIRFG