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
JAI=${RESCAFEHOME}/jai-codec-1.1.3.jar
VERBOSE=0

# Get data file abs paths
n=0
for arg
do
   case $arg in
      -*v*) VERBOSE=1 ;;
      -*) # Skip other command line opts
         ;;
      /*) # An abs path - keep it
         resfile[$n]=$arg ;;
      *) 
         # A rel path - convert to abs
         resfile[$n]=$PWD/$arg
   esac

   n=$((n+1))
done

CLASSPATH=${JAI}:${JIMI}:${RESCAFEHOME}/ResCafe.jar:${RESCAFEHOME}/plugins
#cd $RESCAFEHOME


if [ $VERBOSE -eq 0 ]
then
   java -cp $CLASSPATH ResCafe ${resfile[*]} >/dev/null 2>/dev/null
else
   java -cp $CLASSPATH ResCafe ${resfile[*]}
fi

