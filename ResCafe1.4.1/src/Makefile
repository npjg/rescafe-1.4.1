# $Header$ 
#

# $Log$
#

#----------------------------------------------------------------------------
.SUFFIXES: .class .java .jar


JAVAC = javac
#JAVAC=jikes

APPCLASSES=ResourceManager Colors Jarload Handlers \
	FilePicker.class JFilePicker.class \
        MacBinaryHeader.class \
	DocumentManager.class \
	FileController.class \
	RGBColorNames.class XpmImage.class \
	MacResTypeListModel.class IconTable.class \
	jHandlerView.class \
	RCSplashWindow.class \
	jMainResourceView.class ResCafe.class ResPresso.class

PLUGINS=MacResourceHandler.class \
	CURSResourceHandler.class \
	SICNResourceHandler.class \
	STR_ResourceHandler.class \
	ICN_maskResourceHandler.class \
	acurResourceHandler.class \
	ICONResourceHandler.class \
	clutResourceHandler.class \
	icl4ResourceHandler.class \
	icl8ResourceHandler.class \
	ics4ResourceHandler.class \
	ics8ResourceHandler.class \
	ics_maskResourceHandler.class \
	icnsResourceHandler.class

DO_JAR_COMPILE := $(shell if [ -f MultiClassLoader.java -a \
                               -f JarClassLoader.java -a \
                               -f JarResources.java ]; \
                          then echo "y" ; fi)

VPATH=ResourceManager:.
CLASSDIR=.

#----------------------------------------------------------------------------
all: ResCafe.jar

ResCafe.jar: app
	jar -c0f ResCafe.jar *.class ResourceManager/*.class *.gif

app: ${APPCLASSES}

.PHONY : ResourceManager
ResourceManager :
	$(MAKE) all -C ResourceManager


Colors: MacStandard16Palette.class MacStandard256Palette.class

Dialogs: MessageDialog.class 

#????????????????????????????????????????????????????????????????????????????
# Tried to "detect" and make or not make Jar support
# BUT this fouls up HandlerTable compile...
ifeq ("$(DO_JAR_COMPILE)", "y")
Jarload: JarResources.class MultiClassLoader.class JarClassLoader.class
else
Jarload:
endif
#????????????????????????????????????????????????????????????????????????????

Handlers: MacResourceHandler.class HandlerTable.class DefaultResourceHandler.class

plugins: app
	$(MAKE) -C plugins


.java.class:
	${JAVAC} -classpath .:/opt/java/classes/JimiProClasses.zip:/opt/jdk/jre/lib/rt.jar:${CLASSPATH} -d ${CLASSDIR}  $<

clean:  
	rm -f *.class
	$(MAKE) clean -C ResourceManager

