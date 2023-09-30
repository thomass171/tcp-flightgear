#!/bin/bash
#
# build and deploy everything like described in README.md
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

validateHOSTDIRFG

#sh bin/deployBundle.sh data
#checkrc deployBundle

mvn install
checkrc mvn

#sh bin/deploy.sh
#checkrc deploy

#sh bin/deployBundle.sh  -m engine
#checkrc deployBundle

# No C# for now
#for m in ...
#do
        #zsh bin/java2cs.sh $m
        #checkrc java2cs $m
#done
