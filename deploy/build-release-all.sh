#!/usr/bin/env bash

echo 'building backend...'
export LEIN_SNAPSHOTS_IN_RELEASE=1
lein do clean, uberjar
cp .lein-env target/
cp twitter-security.edn target/
cp twitter-settings.edn target/
cp classify-settings.edn target/
echo 'backend was built'

echo 'building frontend...'

echo 'Cleaning assets'
rm -rf release/vendor
rm -rf release/css
rm -rf release/font-awesome
rm -rf release/images

echo 'Copying resources'
cp -r resources/public/css release/css
cp -r resources/public/vendor release/vendor
cp -r resources/public/font-awesome release/font-awesome
cp -r resources/public/images release/images

echo 'Building js app'
lein cljsbuild once release
echo 'tech-radar was build'

