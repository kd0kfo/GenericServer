#!/bin/sh
cd src
echo build_date=$(date +"%Y-%m-%d %H:%M") > com/davecoss/android/genericserver/build.info
CLASSPATH=.:$(pwd)
(cd com/davecoss/android/genericserver/ && javac -cp $CLASSPATH $(\ls *.java|grep -v Console) ) && jar cfm GenericServer.jar Manifest.txt com org && mv GenericServer.jar ..
