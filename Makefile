# The default build target if make is run with no arguments, points to the DBInterface.class target
all: DBInterface.class

# Main target that compiles our .java file
DBInterface.class: DBInterface.java
	javac -cp mssql-jdbc-11.2.0.jre18.jar DBInterface.java

# Builds and then runs in one command
run: DBInterface.class
	java -cp .:mssql-jdbc-11.2.0.jre18.jar DBInterface

# Cleans up build output files (just .class files for java)
clean:
	rm -f *.class
