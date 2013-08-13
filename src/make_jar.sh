CLASSPATH=.:$(pwd)
(cd com/davecoss/android/genericserver/ && javac -cp $CLASSPATH {GenericServer,ServerBundle,Standalone}.java) && jar cfm GenericServer.jar Manifest.txt com org


