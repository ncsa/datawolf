#!/bin/sh

# exit on error, with error code
set -e

# use DEBUG=echo ./release.sh to print all commands
export DEBUG=${DEBUG:-""}

${DEBUG} docker build --tag datawolf/datawolf:latest .
