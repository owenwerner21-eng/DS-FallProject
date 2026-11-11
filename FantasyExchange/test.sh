#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build
javac -Xlint:all -cp acm.jar -d build ./*.java tests/ExchangeTest.java
java -cp "build:acm.jar" ExchangeTest
