
all:
	./make_jar.sh

clean:
	rm -f GenericServer.jar src/com/davecoss/android/genericserver/*.class

%.jks:
	keytool -genkey -keystore $@ -keyalg RSA
