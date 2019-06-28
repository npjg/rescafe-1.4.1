# $Header$ 
#

# $Log$
#

#----------------------------------------------------------------------------
.SUFFIXES: .class .java


JAVAC = javac 
#JAVAC = jikes
VPATH = .:ResourceManager

#----------------------------------------------------------------------------
all: ResourceManager

ResourceManager : ResourceIntegers.class Resource.class ResourceType.class ResourceModel.class

.java.class:
	${JAVAC} -classpath .:..:/opt/jdk/jre/lib/rt.jar:${CLASSPATH}   $<

clean:  
	rm -f *.class
