#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
find src test -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d out
java -cp out setup.TestRunner
