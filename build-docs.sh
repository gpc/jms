#!/usr/bin/env bash

set -e

export KINDLEGEN=`pwd`/kindlegen/kindlegen
./gradlew docs --stacktrace