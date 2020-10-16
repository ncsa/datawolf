#!/bin/sh

# Exit on error
set -e

# use DEBUG=echo ./release-all.sh to print all commands
export DEBUG=${DEBUG:-""}

# Find out what branch we are on
BRANCH=${BRANCH:-"$(git rev-parse --abbrev-ref HEAD)"}

# use SERVER=XYZ/ to push to a different server
SERVER=${SERVER:-""}

# find out the version
if [ "${BRANCH}" = "master" ]; then
    VERSION=${VERSION:-""}
    if [ "${VERSION}" = "" ]; then
      TMPVERSION=$(cat pom.xml | grep "<version>.*</version>" | head -1 |awk -F'[><]' '{print $3}')
        echo "Detected version ${TMPVERSION}"
        VERSION="latest"
        OLDVERSION=""
        while [ "$OLDVERSION" != "$TMPVERSION" ]; do
            VERSION="${VERSION} ${TMPVERSION}"
            OLDVERSION="${TMPVERSION}"
            TMPVERSION=$(echo ${OLDVERSION} | sed 's/\.[0-9]*$//')
        done
    fi
elif [ "${BRANCH}" = "develop" ]; then
    VERSION="develop"
else
    exit 0
fi

# tag and push all images
for i in datawolf; do
    for v in ${VERSION}; do
	if [ "$v" != "latest" -o "$SERVER" != "" ]; then
            ${DEBUG} docker tag datawolf/${i}:latest ${SERVER}datawolf/${i}:${v}
        fi
	${DEBUG} docker push ${SERVER}datawolf/${i}:${v}
    done
done
    
