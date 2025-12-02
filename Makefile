# The default build target if make is run with no arguments, points to the DBInterface.class target
all: DBInterface.class

# Main target that compiles our .java file
DBInterface.class: DBInterface.java
	javac -cp sqlite-jdbc-3.51.1.0.jar DBInterface.java

# Builds and then runs in one command
run: DBInterface.class
	java -cp .:sqlite-jdbc-3.51.1.0.jar DBInterface

# Cleans up build output files (just .class files for java)
clean:
	rm -f *.class
