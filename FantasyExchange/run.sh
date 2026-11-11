#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
javac -cp acm.jar -d build ./*.java
java -cp "build:acm.jar" FantasyExchangeGUI
