#!/bin/bash 

function run {
  cd ..
  if [ -f backend-release.zip ]; then
    rm backend-release.zip
  fi

  ./build-release-all.sh
  echo "creating backend archive for uploading..."
  zip backend-release.zip -r backend-release
  echo "uploading backend to server..."
  scp backend-release.zip $SCPTECHRADAR 

  echo "fetching the latest version of techradar.github.io..."
  cd ../tech-radar.github.io
  git --git-dir=./.git --work-tree=./ fetch origin master
  git --git-dir=./.git --work-tree=./ reset --hard origin/master


  echo "preparing frontend for uploading..."
  cd ../tech-radar/frontend-release
  cp -r css font-awesome images index.html main.js vendor ../../tech-radar.github.io
  cd ../../tech-radar.github.io

  echo "committing & pushing frontend..."
  git --git-dir=./.git --work-tree=./ commit -a -m "auto commit from deploy script"
  git --git-dir=./.git --work-tree=./ push origin master 
}

function error {
  echo "you must provide a var called SCPTECHRADAR (for example, username@127.0.0.1:~)"
}

if [ -z $SCPTECHRADAR ]; then 
  error
else 
  run
fi

