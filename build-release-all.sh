#!/usr/bin/env bash

echo 'building backend...'
export LEIN_SNAPSHOTS_IN_RELEASE=1
rm -rf backend-release/
lein do clean, uberjar
mkdir backend-release/
cp .lein-env backend-release/
cp twitter-security.edn backend-release/
cp twitter-settings.edn backend-release/
cp classify-settings.edn backend-release/
cp hashtag-filter-settings.edn backend-release/
cp target/backend.jar backend-release/
echo 'backend was built'

echo 'building frontend...'

echo 'Cleaning assets'
rm -rf frontend-release/vendor
rm -rf frontend-release/css
rm -rf frontend-release/font-awesome
rm -rf frontend-release/images

echo 'building design files'
lein sass once

echo 'Copying resources'
cp -r resources/public/css frontend-release/css
cp -r resources/public/vendor frontend-release/vendor
cp -r resources/public/font-awesome frontend-release/font-awesome
cp -r resources/public/images frontend-release/images

echo 'Building js app'
lein cljsbuild once release
echo 'tech-radar was build'

