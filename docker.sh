#!/bin/sh

# exit on error, with error code
set -e

# use new docker build options
export DOCKER_BUILDKIT=1

# use DEBUG=echo ./release.sh to print all commands
export DEBUG=${DEBUG:-""}

${DEBUG} docker build --progress=plain --tag datawolf/datawolf:latest .
