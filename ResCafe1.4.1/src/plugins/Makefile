.SUFFIXES: .class .java


JAVAC = jikes

AUX=GBS_ImageResourceHandler.class

PLUGINS=BNDLResourceHandler.class \
	CURSResourceHandler \
	FREFResourceHandler.class \
	IconFamilyResourceHandler \
	ICONResourceHandler \
	MENUResourceHandler \
	PAT_ResourceHandler \
	SICNResourceHandler \
	STR_ResourceHandler.class \
	acurResourceHandler.class \
	cicnResourceHandler \
	clutResourceHandler.class \
	icnsResourceHandler.class \
	plttResourceHandler \
        versResourceHandler.class

#	icl4ResourceHandler.class \
#	icl8ResourceHandler.class \
#	ics4ResourceHandler.class \
#	ics8ResourceHandler.class \
#	ics_maskResourceHandler.class \
#	ICN_maskResourceHandler \

#--------------------------------------------------------------------------
all: aux  plugins

aux: ${AUX}

plugins: ${PLUGINS}

CURSResourceHandler: IconRenderer.class CURSResourceHandler.class 

ICN_maskResourceHandler: IconRenderer.class ICN_maskResourceHandler.class

ICONResourceHandler: IconRenderer.class ICONResourceHandler.class

IconFamilyResourceHandler: IconRenderer.class GBS_ImageResourceHandler.class IconFamilyResourceHandler.class

MENUResourceHandler: MENURecord.class MENUResourceHandler.class 

PAT_ResourceHandler: IconRenderer.class PAT_ResourceHandler.class

SICNResourceHandler: IconRenderer.class SICNResourceHandler.class  

cicnResourceHandler: IconRenderer.class cicnParser.class cicnResourceHandler.class 

plttResourceHandler: Palette.class plttResourceHandler.class


.java.class:
	${JAVAC} -classpath .:..:/opt/java/classes/JimiProClasses.zip:/opt/jdk/jre/lib/ext/jai_codec.jar:/opt/jdk/jre/lib/rt.jar:${CLASSPATH}   $<


clean:  
	rm *.class
