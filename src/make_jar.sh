(cd com/davecoss/android/genericserver/ && javac -cp ~/src/json-simple-1.1.1.jar {GenericServer,ServerBundle,Standalone}.java)
jar cfm GenericServer.jar Manifest.txt com
