#!/bin/bash
#
# build and deploy everything like described in README.md
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

validateHOSTDIRFG

# Some unit tests need deployed bundles, so first build without tests.
mvn clean install -DskipTests=true
checkrc mvn

sh bin/deployBundles.sh
sh bin/mkTerraSyncBundle.sh

mvn install
checkrc mvn

sh bin/deploy.sh
checkrc deploy

# No C# for now
#for m in ...
#do
        #zsh bin/java2cs.sh $m
        #checkrc java2cs $m
#done
