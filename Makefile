default:
	javac -Xlint:deprecation ClosureCompilerRecursive.java

test:
	cd tests && ./run
