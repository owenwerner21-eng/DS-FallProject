#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/app
javac -Xlint:all -cp acm.jar -d build/app ./*.java
printf 'Manifest-Version: 1.0\nMain-Class: FantasyExchangeGUI\nClass-Path: acm.jar\n\n' > build/MANIFEST.MF
jar cfm FantasyExchange.jar build/MANIFEST.MF -C build/app .
echo 'Built FantasyExchange.jar. Keep acm.jar beside it.'
