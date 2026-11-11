#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p out
find src -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d out
echo 'Starting Build Your Setup at http://localhost:8080'
exec java -cp out setup.web.WebServer 8080
