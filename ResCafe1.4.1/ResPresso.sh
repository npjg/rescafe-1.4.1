#!/bin/sh

# $Header: /home/gbsmith/projects/ResCafe/ResCafe_devel/RCS/ResCafe.sh,v 1.2 2000/07/15 21:25:39 gbsmith Exp gbsmith $
#----------------------------------------------------------------------------
# $Log: ResCafe.sh,v $
# Revision 1.2  2000/07/15 21:25:39  gbsmith
# Convert args to abs paths then pass to ResCafe
#
# Revision 1.1  2000/07/15 20:48:25  gbsmith
# Initial revision
#
#
#----------------------------------------------------------------------------

RESCAFEHOME=.
JIMI=/opt/java/classes/JimiProClasses.zip
JAI=/opt/jdk/jre/lib/ext/jai_codec.jar


CLASSPATH=${JAI}:${JIMI}:${RESCAFEHOME}/ResCafe.jar:${RESCAFEHOME}/plugins

java -cp $CLASSPATH ResPresso $* 
