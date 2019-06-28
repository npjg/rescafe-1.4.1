/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/ResPresso.java,v 1.2 2000/12/12 07:52:03 gbsmith Exp $ */

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import ResourceManager.*;

// ResPresso
/*=======================================================================*/
/* Copyright (c) 2000 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: ResPresso.java,v $
 * Revision 1.2  2000/12/12 07:52:03  gbsmith
 * Made some changes to the BASIC type list mode
 *
 * Revision 1.1  2000/12/11 07:46:01  gbsmith
 * Initial revision
 *
 *
 */

/*=======================================================================*/
public class ResPresso
{
   // ResPresso is intended to a command line alternative to ResCafé that
   // can conveniently access the contents of resource files much like
   // the original 'macfork' app. We would like to use as much of the
   // ResCafé codebase as possible, mainly the core ResourceManager and
   // the type plugins. One problem is a plugin subclasses JPanel, however
   // if if this is not a GUI app, we should be able to make use of the
   // type handling functionality without ever calling display().

   // Possible command-line switches:
   // ResPresso somefile.rsrc  = just list types (and counts?)
   //           -t "typ1"      = only work with given type
   //           -g "Icons"     = only work with given type group (hardcoded)?
   //           -n X           = only work with given res IDs
   //           -l             = list resources
   //           -s             = save resources
   //           -r             = raw mode (don't use a handler)
   //           -h             = handle mode (only save types with a handler)
   //           -d <somedir>   = directory to save in(default is infile_export)
   //           -f             = force, i.e. ask NO questions
   //           -q             = quiet mode
   //

   //           -d <somedir>   = directory to save in(default is infile_export)
   //           -f             = force, i.e. ask NO questions
   //           -q             = quiet mode
   //
   //   -l, -s, and -r would be mutually exclusive; last one on CL wins
   //
   // So:
   //   ResPresso -l -t "icl4" "MENU"  would list all res of those types ONLY
   //   ResPresso -s -t "icl4" "MENU"  would save all res of those types ONLY
   //   ResPresso -s -t "icl8" -d asdf would save all icl8's in dir ./asdf
   //   ResPresso -f -s -t "icl8" -d asdf would do same and blow files/dirs away

   /*--- Data -----------------------------------------------------------*/
   DocumentManager   mydocmgr;
   HandlerTable      myhandlers;
   FileController    myfctrl;
   File              srcFile;
   RandomAccessFile  raf;
   ResourceModel     resmod;

   Vector            inTypes;
   String            inFilename;
   String            outDir;
   int               outMode;
   boolean           doForce;
   boolean           verbose;

   MacResourceHandler currentHandler;

   static final int BASIC   = 0;
   static final int LIST    = 1;
   static final int SAVE    = 2;
   static final int RAW     = 3;
   static final int HANDLED = 4;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: ResPresso.java,v 1.2 2000/12/12 07:52:03 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public static void main( String args[] )
   {
      ResPresso app = new ResPresso();
      app.processArgs(args);

      // Load files if given a names on the command-line
      if(app.inFilename == null)
      {
         app.usage();
         System.exit(1);
      }

      app.go();
      System.exit(0);
   }

   /*--------------------------------------------------------------------*/
   void go()
   {
      //dumpArgs();

      try { load(); }
      catch(IOException ioe) {}

      switch(outMode)
      {
         case  HANDLED: saveHandled(); break;
         case  RAW:     saveRaw();     break;
         case  SAVE:    save();        break;
         case  LIST:    listTypes();   break;
         case  BASIC:   list();        break;
         default:       list();        break;
      }      
   }

   /*--------------------------------------------------------------------*/
   public ResPresso()
   {
      resmod  = new ResourceModel();
      inTypes = new Vector();
      outDir  = null;
      outMode = BASIC;
      doForce = false;
      verbose = true;
   }

   /*--------------------------------------------------------------------*/
   void dumpArgs()
   {
      System.out.println("#----------------------------------------");
      System.out.println("resmod  = " + resmod);
      System.out.println("inTypes = " + inTypes.size());
      System.out.println("          " + inTypes);
      System.out.println("outDir  = " + outDir);
      System.out.println("outMode = " + outMode);
      System.out.println("doForce = " + doForce);
      System.out.println("verbose = " + verbose);
      System.out.println("#----------------------------------------");
   }

   /*--------------------------------------------------------------------*/
   void processArgs(String inArgs[])
   {
      for(int i=0; i < inArgs.length; i++)
      {
         if(inArgs[i].startsWith("-")) // Command line switch
         {
            if(inArgs[i].indexOf('r') > -1) outMode = RAW;
            if(inArgs[i].indexOf('s') > -1) outMode = SAVE;
            if(inArgs[i].indexOf('l') > -1) outMode = LIST;
            if(inArgs[i].indexOf('f') > -1) doForce = true;
            if(inArgs[i].indexOf('q') > -1) verbose = false;

            // Following must be at end of an arg
            if     (inArgs[i].endsWith("d")) outDir = inArgs[++i];
            else if(inArgs[i].endsWith("t")) inTypes.add(inArgs[++i]);
            //else if(inArgs[i].endsWith("n")) resid???;
            //else if(inArgs[i].endsWith("g")) groups???;
         } else
            inFilename = inArgs[i];
      }
   }

   /*--------------------------------------------------------------------*/
   void list()
   {
      String theTypes[];

      theTypes = resmod.getTypeArray();
      if(theTypes == null) return;

      Arrays.sort(theTypes);
      for(int t=0; t < theTypes.length; t++)
      {
         System.out.println("Type\t'" + theTypes[t] + "' \t" +
                            resmod.getCountOfType(theTypes[t]));
      }
   }

   /*--------------------------------------------------------------------*/
   void listTypes()
   {
      // Change this
      String listTypes[];
      ResourceType rt;
      Resource res[];

      listTypes = getTypes();

      for(int t=0; t < listTypes.length; t++)
      {
         System.out.println("Type\t'" + listTypes[t] + "' \t" +
                            resmod.getCountOfType(listTypes[t]));
         rt = resmod.getResourceType(listTypes[t]);
         res = rt.getResArray();
         for(int r=0; r < rt.size(); r++)
         {
            System.out.print("\t" + res[r].getID());
            System.out.print("\t" + res[r].size());
            if(res[r].getName() != null)
               System.out.print("\t\"" + res[r].getName() + "\"");
            System.out.println();
         }
      }
   }

   /*--------------------------------------------------------------------*/
   void print() { resmod.print(System.out); }

   /*--------------------------------------------------------------------*/
   String[] getTypes()
   {
      String outTypes[];

      if(inTypes.size() > 0)
      {
         outTypes = new String[inTypes.size()];
         inTypes.toArray(outTypes);
      } else {
         outTypes = resmod.getTypeArray();
         if(outTypes == null) outTypes = new String[0];
         else Arrays.sort(outTypes);
      }
      return outTypes;
   }

   /*--------------------------------------------------------------------*/
   File getDir()
   {
      String saveDirname;
      File saveDir;
      char ans;

      if(outDir != null) saveDirname = outDir;
      else saveDirname = inFilename + "_export";

      saveDir = new File(saveDirname);
      if(saveDir.exists())
      {
         if(saveDir.isDirectory()) // Is it a dir? Ask to use
         {
            if(doForce) ans = 'y';
            else
               try
               {
                  System.out.println("Directory '" + saveDirname + "'");
                  System.out.print("already exists - use it? [n] ");
                  ans = (char)System.in.read();
               } catch(IOException ioe) { return null; }

            if(ans != 'y' && ans != 'Y') return null;
         } else { // Is it something else? Ask to blow it away
            if(doForce) ans = 'y';
            else
               try
               {
                  System.out.println("" + saveDirname + "' already exists ");
                  System.out.print("and is not a directory - overwrite? [n] ");
                  ans = (char)System.in.read();
               } catch(IOException ioe) { return null; }

            if(ans != 'y' && ans != 'Y') return null;

            saveDir.delete();
            if(!saveDir.mkdir())
            {
               System.err.println("ERROR: Could not make directory");
               return null;
            }
         }
      } else {
         if(!saveDir.mkdir())
         {
            System.err.println("ERROR: Could not make directory");
            return null;
         }
      }

      return saveDir;
   }

   /*--------------------------------------------------------------------*/
   void save()
   {
      File saveDir, typeDir;
      String saveTypes[];

      // Check directory integrity
      saveDir = getDir();
      if(saveDir == null) { System.err.println("Save FAILED"); return; }

      // Load up the type handlers (didn't need them 'til now)
      myhandlers = new HandlerTable();
      myhandlers.setVerbosity(false);
      myhandlers.build();

      saveTypes = getTypes();

      for(int t=0; t < saveTypes.length; t++)
      {
         if(!resmod.contains(saveTypes[t]))
         {
            System.err.println("ERROR: Type '" + saveTypes[t] + 
                               "' not available ");
            continue;
         }

         // Instantiate handler
         if(myhandlers.canHandleType(saveTypes[t]))
         {
            try
            {
               currentHandler =
                  (MacResourceHandler)myhandlers.
                  getHandler(saveTypes[t]).newInstance();
            } catch (Exception e) {
               System.err.println(e);
            }
         } else currentHandler = new DefaultResourceHandler();

         typeDir = new File(saveDir, saveTypes[t]);
         if(typeDir.exists() && !typeDir.isDirectory()) typeDir.delete();
         if(!typeDir.exists()) typeDir.mkdir();

         currentHandler.setResData(resmod.getResourceType(saveTypes[t]));
         currentHandler.setResModel(resmod);
         currentHandler.init();
         currentHandler.save(typeDir);
      }
   }

   /*--------------------------------------------------------------------*/
   void saveRaw()
   {
      File saveDir, typeDir;
      String saveTypes[];

      // Check directory integrity
      saveDir = getDir();
      if(saveDir == null) { System.err.println("Save FAILED"); return; }

      saveTypes = getTypes();

      for(int t=0; t < saveTypes.length; t++)
      {
         if(!resmod.contains(saveTypes[t]))
         {
            System.err.println("ERROR: Type '" + saveTypes[t] + 
                               "' not available ");
            continue;
         }

         // Instantiate handler
         currentHandler = new DefaultResourceHandler();
         
         typeDir = new File(saveDir, saveTypes[t]);
         if(typeDir.exists() && !typeDir.isDirectory()) typeDir.delete();
         if(!typeDir.exists()) typeDir.mkdir();

         currentHandler.setResData(resmod.getResourceType(saveTypes[t]));
         currentHandler.setResModel(resmod);
         currentHandler.init();
         currentHandler.save(typeDir);
      }
   }

   /*--------------------------------------------------------------------*/
   void saveHandled()
   {
      File saveDir, typeDir;
      String saveTypes[];

      // Check directory integrity
      saveDir = getDir();
      if(saveDir == null) { System.err.println("Save FAILED"); return; }

      // Load up the type handlers (didn't need them 'til now)
      myhandlers = new HandlerTable();
      myhandlers.setVerbosity(false);
      myhandlers.build();

      saveTypes = getTypes();

      for(int t=0; t < saveTypes.length; t++)
      {
         if(!resmod.contains(saveTypes[t]))
         {
            System.err.println("ERROR: Type '" + saveTypes[t] + 
                               "' not available ");
            continue;
         }

         // Instantiate handler
         if(myhandlers.canHandleType(saveTypes[t]))
         {
            try
            {
               currentHandler =
                  (MacResourceHandler)myhandlers.
                  getHandler(saveTypes[t]).newInstance();
            } catch (Exception e) {
               System.err.println(e);
            }

            typeDir = new File(saveDir, saveTypes[t]);
            if(typeDir.exists() && !typeDir.isDirectory()) typeDir.delete();
            if(!typeDir.exists()) typeDir.mkdir();
            
            currentHandler.setResData(resmod.getResourceType(saveTypes[t]));
            currentHandler.setResModel(resmod);
            currentHandler.init();
            currentHandler.save(typeDir);
         } else
            System.err.println("No plugin for type '" + saveTypes[t] + "'");
      }
   }

   /*--------------------------------------------------------------------*/
   void load() throws IOException
   {
      resmod.init();

      srcFile = new File(inFilename);
      if(!srcFile.exists())
      {
         System.err.println("ERROR: " + inFilename + " does not exist");
         return;
      }

      if(!srcFile.isFile())
      {
         System.err.println("ERROR: " + inFilename + " is not a file");
         return;
      }

      raf = new RandomAccessFile(srcFile, "r");

      resmod.setFilename(inFilename);

      // Check to see if this is a MacBinary file
      MacBinaryHeader mbh = new MacBinaryHeader();
      mbh.read(raf);
      if(mbh.validate())
      {
         // This is a MacBinary file - must always seek to ResFork
         raf.seek(mbh.getResForkOffset());
         resmod.read(raf, mbh.getResForkOffset());
      } else {
         // ASSUME an extracted Resource Fork - must always seek to top
         raf.seek(0);
         resmod.read(raf);
      }

      raf.close();
   }

   /*--------------------------------------------------------------------*/
   void usage()
   {
      System.err.println("Usage: ResPresso [mode] [options] resfile");
      System.err.println();
      System.err.println("Modes (one only):");
      System.err.println("  (none)   \tList type signatures only");
      System.err.println("  -l       \tList resources - type, id, size, name");
      System.err.println("  -s       \tSave resources - uses plugins when present");
      System.err.println("  -r       \tSave raw resources - saves bytes only");
      System.err.println("  -h       \tSave parsed resources - only if plugin available");
      System.err.println();
      System.err.println("Options:");
      System.err.println("  -f       \tForce - ask no questions(overwrite, delete, etc.)");
      System.err.println("  -q       \tQuiet - less verbosity");
      System.err.println("  -d <dir> \tSave to designated directory instead of default");
      System.err.println("  -t <TYPE>\tOnly work with given type");
      System.err.println("           \tCan use multiple times");
      System.err.println("Example:");
      System.err.println("  % ResPresso -s -t \"icl4\" -t \"MENU\" -d qwerty A_Mac_App.rsrc");
      System.err.println("\twould save only types \"icl4\" & \"MENU\" from file 'A_Mac_App.rsrc' to");
      System.err.println("\tdirectory 'qwerty'. If a file 'qwerty' exists, you would be asked if");
      System.err.println("\tit can be deleted.");
   }
}
