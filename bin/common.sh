#
# Only extensions of tcp-22/bin/common.sh
#
export VERBOSELOG=0

export CLASSPATH

export TCP22DIR=$OWNDIR/../../tcp-22
source $TCP22DIR/bin/common.sh || exit 1
PROJECT_HOME=$(dirname $(realpath $OWNDIR))
echo PROJECT_HOME=$PROJECT_HOME
# make TCP22DIR absolute
TCP22DIR=$PROJECT_HOME/../tcp-22

for module in flightgear tools-fg
do
  CLASSPATH=$CLASSPATH:$MR/de/yard/tcp-flightgear/module-$module/$VERSION/module-$module-$VERSION.jar
done

validateHOSTDIRFG() {
  if [ -z "$HOSTDIRFG" ]; then
    error "HOSTDIRFG not set"
  fi
  if [ ! -d "$HOSTDIRFG" ]; then
    error "HOSTDIRFG $HOSTDIRFG not found"
  fi
}


