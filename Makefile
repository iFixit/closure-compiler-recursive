default: ClosureCompilerRecursive.class
	javac -Xlint:deprecation ClosureCompilerRecursive.java

test:
	cd tests && bash -ex ./run
