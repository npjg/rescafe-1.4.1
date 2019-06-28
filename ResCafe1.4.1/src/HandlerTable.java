/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/HandlerTable.java,v 1.12 2000/12/12 19:46:17 gbsmith Exp $ */

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.StreamTokenizer;
import java.io.StringReader;

import java.net.URL;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Vector;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: HandlerTable.java,v $
 * Revision 1.12  2000/12/12 19:46:17  gbsmith
 * Class can now locate the main plugin dir based on own location;
 * Also scans CLASSPATH for additional plugin dirs
 *
 * Revision 1.11  2000/12/11 02:39:17  gbsmith
 * Added setVerbosity() accessor method to supress reports easily;
 * General code clean-up and reordering
 *
 * Revision 1.10  2000/11/27 19:53:17  gbsmith
 * Reworked verbose output formatting to work with new splash screen observer.
 *
 * Revision 1.9  2000/05/24 06:50:29  gbsmith
 * Made observable so it can notify Windows and stuff when it has
 * been updated, such as with a rescan.
 *
 * Revision 1.8  1999/10/27 07:15:38  gbsmith
 * Implemented Runnable interface so loading can be put in a thread.
 * Also synchronized build method so only one thread can change it.
 *
 * Revision 1.7  1999/10/22 04:16:32  gbsmith
 * Took Jar class loading BACK out until I can be sure of permission
 * to distribute required classes.
 *
 * Revision 1.6  1999/10/21 22:47:29  gbsmith
 * Added Copyright notice and a few comments.
 *
 * Revision 1.5  1999/10/18 00:07:30  gbsmith
 * Added methods for listing handlers and types supported. These
 * will eventually be moved into more abstract calls for
 * displaying in windows.
 *
 * Revision 1.4  1999/10/13 23:26:31  gbsmith
 * Made Jar file loading optional by adding JarClassLoader detection code.
 * If the loader is not found, the program moves on. Also chopped out some old code.
 *
 * Revision 1.3  1999/10/13 22:55:19  gbsmith
 * Restored support for loading plugin classes from Jar file archives.
 * Uses JarClassLoader class by John D. Mitchell from www.javaworld.com
 *
 * Revision 1.2  1999/10/13 07:51:35  gbsmith
 * Added RCS tags
 *
 */

/*=======================================================================*/
class HandlerTable extends Observable implements Runnable
{
   /*--- Data -----------------------------------------------------------*/
   private Hashtable handlerData;
   private Hashtable handlerTypeList;
   private Class masterClass;
   private String plugDirName = "plugins";
   private boolean VERBOSE = true;
   private String defaultPlugDir = "plugins";
   private Vector pluginDirs;

   /*----- RCS ----------------------------------------------------------*/
   static final String rcsid = "$Id: HandlerTable.java,v 1.12 2000/12/12 19:46:17 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public HandlerTable()
   {
       handlerData = new Hashtable();
       handlerTypeList = new Hashtable();

       try
       {
          masterClass = Class.forName("MacResourceHandler");
       } catch(ClassNotFoundException cnfe) {
          System.err.println(cnfe);
          System.err.println("Master class (MacResourceHandler) " +
                             "registration FAILED... EXITING");
          System.exit(1); // No masterclass? The party's over... )-;
       }
   }

   /*--------------------------------------------------------------------*/
   public void run() { build(); }
   public void setVerbosity(boolean v) { VERBOSE = v; }
   public int getTypeCount( ) { return handlerData.size();  }
   public Enumeration getTypeKeys( )  { return handlerData.keys(); }

   boolean canHandleType(Object tk) { return handlerData.containsKey(tk); }
   Class getHandler(Object tk) { return (Class)handlerData.get(tk); }

   /*--------------------------------------------------------------------*/
   void getPluginDirs()
   {
      URL myURL;
      String myPath;      
      Class me;
      File myFile;
      int retval = 0;
      StreamTokenizer st;

      pluginDirs = new Vector();

      // Find myself (Who am I?! WHERE did I come from?!)
      me = this.getClass();
      myURL = me.getResource(me.getName() + ".class");
      
      // Convert to path - chop off protocol
      myPath = myURL.getFile();
      myPath = myPath.substring(myPath.indexOf(':') + 1);

      // Goto up two levels (self + JAR file)
      myFile = new File(myPath).getParentFile().getParentFile();
      myFile = new File(myFile, "plugins");

      pluginDirs.add(myFile.toString()); // It all seems so convoluted...
      
      // Add other plugin dirs int CLASSPATH
      st = new StreamTokenizer(new StringReader(System.getProperty("java.class.path")));

      st.whitespaceChars(File.pathSeparatorChar, File.pathSeparatorChar);
      st.wordChars(File.separatorChar, File.separatorChar);

      st.wordChars('_', '_');
      st.ordinaryChar('.');
      st.wordChars('.', '.');

      do
      {
         try { retval = st.nextToken(); }
         catch (IOException ioe) {  }
         if(retval != StreamTokenizer.TT_EOF)
         {
            if(st.sval.endsWith("plugins") || 
               st.sval.endsWith("plugins" + File.separator))
               if(!pluginDirs.contains(st.sval)) pluginDirs.add(st.sval);
         }
      } while(retval != StreamTokenizer.TT_EOF);
   }
   

   /*--------------------------------------------------------------------*/
   synchronized void build()
   {
      /* This seems like a lot to synchronize but we really only want
         one thread messing with (esp. rebuilding) the table at a time */
      File currDir;

      getPluginDirs();

      for(int d=0; d < pluginDirs.size(); d++)
      {
         currDir = new File((String)pluginDirs.get(d));
         if(VERBOSE) System.out.println("");
         if(currDir.isDirectory()) processDirectory(currDir);
         else  System.err.println("ERROR: Invalid Plugin Directory - '" +
                                  (String)pluginDirs.get(d) + "'");
      }
      
      setChanged();
      notifyObservers();

      //if(VERBOSE) listHandlersbyType();
   }

   /*--------------------------------------------------------------------*/
   public String getHandlerName(String typekey)
   {
      return handlerData.get(typekey).toString().substring(6);
   }

   /*--------------------------------------------------------------------*/
   public void listHandlersbyType( )
   {
      System.out.println("");
      System.out.println("Can successfully handle these types: ");
      System.out.println("Type ->     Handler");
      System.out.println("----    --------------------------------------");

      Enumeration typekeys = handlerData.keys();
      while(typekeys.hasMoreElements())
      {
         String curkey = (String)typekeys.nextElement();
         System.out.println(curkey + " -> " +
                            handlerData.get(curkey).toString().substring(6));
         // substring chops out "class " prefix
      }
      System.out.println();
   }

   /*--------------------------------------------------------------------*/
   public void listTypesbyHandler( )
   {
      System.out.println("");
      System.out.println("Successfully registered these handlers: ");
      System.out.println("Handler             -> Types ");
      System.out.println("-------------------    ------ ");
      String curtypes[];

      Enumeration handlerkeys = handlerTypeList.keys();
      while(handlerkeys.hasMoreElements())
      {
         String curkey = (String)handlerkeys.nextElement();
         curtypes = (String[])handlerTypeList.get(curkey);

         System.out.print(curkey + " -> ");
         for(int t = 0; t < curtypes.length ; t++)
         {
            System.out.print( curtypes[t] );
            if(t < curtypes.length - 1) System.out.print(", ");
         }
         System.out.println();
      }
      System.out.println();
   }

   /*--------------------------------------------------------------------*/
   void processDirectory( File searchdir)
   {
      String plugfiles[], plugdirs[], jarfiles[], supportedTypes[];
      String strippedClass;
      Class currentClass;
      StringBuffer foundStr;

      SubdirClassLoader sdcl = new SubdirClassLoader(searchdir);
      if(VERBOSE) System.out.println("----- ENTER " + searchdir +
                                     "-----------------------------------");

      //--------------------------------------------------------------------
      // Probe class files
      if(VERBOSE) System.out.print("Probing class files in " +
                                     searchdir + "...");
      plugfiles = searchdir.list( new ClassFileFilter() );
      if(VERBOSE)
         if(plugfiles.length > 0) System.out.println("found " + plugfiles.length);
         else                     System.out.println("none.");

      for(int i=0; i < plugfiles.length; i++)
      {
         // Chop out '.class' extension
         strippedClass = plugfiles[i].substring(0, plugfiles[i].length() - 6);
         try
         {
            String searchname = searchdir.getName();
            currentClass = sdcl.loadClass(strippedClass);

            foundStr = new StringBuffer("" + currentClass);
            if(checkSuperclass(currentClass))
            {
               // Class is what we want...
               MacResourceHandler mrh =
                  (MacResourceHandler)currentClass.newInstance();
               supportedTypes =  mrh.getTypes();
               foundStr.append(":");
               for(int k = 0; k < supportedTypes.length; k++)
               {
                  foundStr.append("  " + supportedTypes[k]);

                  // Allow special 'default' signature to replace built-in
                  if(supportedTypes[k].compareTo("default") == 0)
                     handlerData.put("default", currentClass);

                  // Valid types are 4 chars long
                  if(supportedTypes[k].length() == 4)
                     handlerData.put(supportedTypes[k], currentClass);

                  // else drop invalid types
               }

               // Also record which types a handler accepts
               // in another hash for reporting
               handlerTypeList.put(currentClass.toString().substring(6),
                                   supportedTypes);
               // PROBLEM: This also records invalid types...

            }

            setChanged();
            notifyObservers(foundStr.toString());

            // Report
            if(VERBOSE)
            {
               System.out.print("\t" + foundStr);
               System.out.flush( );
            }

         } catch(Exception whatever) {
            // System.err.println(whatever);
         }
         if(VERBOSE) System.out.println("");
      }
      if(VERBOSE) System.out.println("");

      //--------------------------------------------------------------------
      // Probe jar files
      if(VERBOSE) System.out.print("Probing jar files in " +
                                   searchdir + "...");
      boolean canLoadJar = true;
      try
      {
         /* These are classes from http://www.javaworld.com
            written by John D. Mitchell (and others I think).

            I am trying to make these optional in case they
            can't be distributed with this app due to
            copyright restrictions.
            I am working on getting approval to package these
            classes with ResCafé.
         */
         Class.forName("MultiClassLoader");
         Class.forName("JarResources");
         Class.forName("JarClassLoader");
      } catch (ClassNotFoundException cnfe) {
         canLoadJar = false;
      }

      // Taking jar loading out because it (or rather lack of
      // MultiClassLoader, JarResources, JarClassLoader)
      // screws up this compile
      /*
      if(canLoadJar)
      {
         JarResources currentJar;
         JarClassLoader jcl;
         Enumeration jcEnum;

         jarfiles = searchdir.list( new JarFileFilter() );

         if(jarfiles.length > 0)
            System.out.println("found " + jarfiles.length);
         else
            System.out.println("none.");

         // Loop through jar files
         for(int i=0; i < jarfiles.length; i++)
         {
            if(VERBOSE) System.out.println("JAR " + i + ") " + jarfiles[i]);
            currentJar = new JarResources( plugDirName + File.separator +
                                           jarfiles[i] );
            jcl = new JarClassLoader(currentJar);
            jcEnum = currentJar.classKeys();

            // Loop through files in current jar file
            while (jcEnum.hasMoreElements())
            {
               currentClass  = null;
               strippedClass = jcEnum.nextElement().toString();
               strippedClass =
                  strippedClass.substring(0, strippedClass.length() - 6);

               try
               {
                  currentClass = jcl.loadClass( strippedClass, true);
               } catch(ClassNotFoundException cnfe) {
                  System.err.println(cnfe);
               }

               if(VERBOSE) System.out.print("\t" + currentClass );

               // Copied try block from above
               try
               {
                  //currentClass = Class.forName(strippedClass);
                  if(checkSuperclass(currentClass))
                  {
                     // Class is what we want...
                     MacResourceHandler mrh =
                        (MacResourceHandler)currentClass.newInstance();
                     supportedTypes =  mrh.getTypes();
                     for(int k = 0; k < supportedTypes.length; k++)
                     {
                        // Report
                        if(VERBOSE)
                        {
                           System.out.print("  " + supportedTypes[k] );
                           System.out.flush();
                        }

                        // Special 'default' signature to replace built-in
                        if(supportedTypes[k].compareTo("default") == 0)
                           handlerData.put("default", currentClass);

                        // Valid types are 4 chars long
                        if(supportedTypes[k].length() == 4)
                           handlerData.put(supportedTypes[k], currentClass);

                        // else drop invalid types
                     }
                  }
               } catch(Exception whatever) {
                  System.err.println(whatever);
               }

               if(VERBOSE) System.out.println("");
            }
         }
      } else System.out.println("UNABLE to load jar files");
      */

      if(VERBOSE) System.out.println("");

      //--------------------------------------------------------------------
      // Probe subdirs
      if(VERBOSE)
         System.out.print("Probing subdirectories in " + searchdir + "... ");
      plugdirs = searchdir.list( new DirFilter() );

      if(VERBOSE)
         if(plugdirs.length > 0) System.out.println("found " + plugdirs.length);
         else                    System.out.println("none.");

      for(int i=0; i < plugdirs.length; i++)
      {
         //if(VERBOSE) System.out.println("DIR " + i + ") " + plugdirs[i]);
         processDirectory( new File( searchdir, plugdirs[i] ) );
      }

      //--------------------------------------------------------------------
      if(VERBOSE)
      {
         System.out.flush();
         System.out.println("----- EXIT " + searchdir +
                            "-----------------------------------");
         System.out.println("");
      }
   }

   /*--------------------------------------------------------------------*/
   boolean checkSuperclass(Class inClass)
   {
      Class supadupa = inClass.getSuperclass();
      while(supadupa != null)
      {
         if(supadupa == masterClass) return true;
         supadupa = supadupa.getSuperclass();
      }

      return false;
   }
}


/*=======================================================================*/
class ClassFileFilter implements FilenameFilter
{
   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: HandlerTable.java,v 1.12 2000/12/12 19:46:17 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public boolean accept(File dir, String name)
   {
      if(!dir.isDirectory() )      return false;
      if(!dir.canRead() )          return false;
      if(!name.endsWith(".class")) return false;
      return true;
   }
}

/*=======================================================================*/
class DirFilter implements FilenameFilter
{
   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: HandlerTable.java,v 1.12 2000/12/12 19:46:17 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public boolean accept(File dir, String name)
   {
      File tmpFile = new File(dir, name);
      if(tmpFile.isDirectory()) return true;
      return false;
   }
}

/*=======================================================================*/
class JarFileFilter implements FilenameFilter
{
   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: HandlerTable.java,v 1.12 2000/12/12 19:46:17 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public boolean accept(File dir, String name)
   {
      if(!dir.isDirectory() )     return false;
      if(!dir.canRead() )         return false;
      if(!name.endsWith(".jar") ) return false;
      return true;
   }
}

/*=======================================================================*/
/* This class is adapted from some other bits and pieces of code from
   http://www.javaworld.com and _Java in a Nutshell_
*/
class SubdirClassLoader extends ClassLoader
{
   /*--- Data -----------------------------------------------------------*/
   File searchdir;

   /*----- RCS ----------------------------------------------------------*/
   static final String rcsid = "$Id: HandlerTable.java,v 1.12 2000/12/12 19:46:17 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public SubdirClassLoader( File indir )
   {
      searchdir = indir;
      // System.out.println("Searchdir = " + searchdir.getPath());
   }

   /*--------------------------------------------------------------------*/
   public synchronized Class loadClass(String className, boolean resolveIt)
      throws ClassNotFoundException
   {
      Class outClass;

      /*-----------------------------------------------------------------*/
      /* Load Attempt 1) Check if we already loaded this class           */
      outClass = findLoadedClass(className);
      if (outClass != null)
      {
         // Return an already-loaded class
         // System.out.println("ClassLoader returning " + className + " at (1)");
         return outClass;
      }

      /*-----------------------------------------------------------------*/
      /* Load Attempt 2) Check if the primordial class loader has it     */
      try
      {
         outClass = super.findSystemClass(className);
         // System.out.println(" ClassLoader returning " +
         //                    className + " at (2)");
         return outClass;
      } catch (ClassNotFoundException ignore_it) { }

      // Filter out system files - leave these to the primordial class loader
      if (className.startsWith("java.")) throw new ClassNotFoundException();

      /*-----------------------------------------------------------------*/
      /* Load Attempt 3) Try to load it from subdirectory 'searchdir'.   */
      byte classData[] = getTypeFromSearchdir(className);
      if ( classData == null ) throw new ClassNotFoundException();

      // Parse class data and resolve
      outClass = defineClass(className, classData, 0, classData.length);
      if ( outClass == null ) throw new ClassFormatError();
      if ( resolveIt )        resolveClass(outClass);

      return outClass;
   }

   /*--------------------------------------------------------------------*/
   private byte[] getTypeFromSearchdir( String className )
   {
      FileInputStream fis;
      String fileName;

      fileName = searchdir.getPath() + File.separatorChar +
      className.replace('.', File.separatorChar) + ".class";

      try { fis = new FileInputStream(fileName); }
      catch (Exception e) { return null; }

      BufferedInputStream   bis = new BufferedInputStream(fis);
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      try
      {
         int nextByte = bis.read();
         while (nextByte != -1)
         {
            out.write(nextByte);
            nextByte = bis.read();
         }
      } catch (IOException ioe) { return null; }

      return out.toByteArray();
   }
}
