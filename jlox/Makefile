
.PHONY run:
run:
	javac -d build *.java
	jar cfm Lox.jar manifest.mf -C build .
	java -jar Lox.jar