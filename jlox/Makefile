
.PHONY run-console:
run-console:
	javac -d build *.java
	jar cfm Lox.jar manifest.mf -C build .
	java -jar Lox.jar


.PHONY run-file:
run-file:
	javac -d build *.java
	jar cfm Lox.jar manifest.mf -C build .
	java -jar Lox.jar testfile