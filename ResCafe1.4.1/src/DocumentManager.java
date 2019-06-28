/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/DocumentManager.java,v 1.3 2000/12/11 02:20:50 gbsmith Exp $ */

import java.io.File;
import java.io.RandomAccessFile;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Hashtable;
import java.util.Set;

import ResourceManager.*;

/*=========================================================================*/
/*
 * $Log: DocumentManager.java,v $
 * Revision 1.3  2000/12/11 02:20:50  gbsmith
 * Changed listDocuments() to sort doc names before returning;
 * Also some general code clean-up
 *
 * Revision 1.2  1999/10/28 05:47:15  gbsmith
 * Added getter method for the name of the current file to
 * enable checkbox menu in viewer.
 *
 * Revision 1.1  1999/10/28 03:54:18  gbsmith
 * Initial revision
 *
 */

/*=========================================================================*/
public class DocumentManager extends Observable
{
   /*--- Data -------------------------------------------------------------*/
   Hashtable     resModTable;
   ResourceModel currentResMod;
   String        currentName;
   FilePicker    myfp;
   HandlerTable  htab;

   /*------ RCS -----------------------------------------------------------*/
   static final String rcsid = "$Id: DocumentManager.java,v 1.3 2000/12/11 02:20:50 gbsmith Exp $";

   /*--- Methods ----------------------------------------------------------*/
   public DocumentManager()
   {
      resModTable = new Hashtable();
      myfp = new JFilePicker();
   }

   /*----------------------------------------------------------------------*/
   public void setHandlers( HandlerTable inHtab ) { htab = inHtab; }
   ResourceModel getCurrent( ) { return currentResMod; }
   String getCurrentName( ) { return currentName;  }

   /*----------------------------------------------------------------------*/
   void saveAll( )
   {
      File typedir, dirToSave;
      String mytype;
      Enumeration typeKeys;
      MacResourceHandler saveHandler = null;

      /*-------------------------------------------------------------------*/
      dirToSave =  myfp.getSaveDir("Save All Types",
                                   currentResMod.getFilename() + "_export");
      if( dirToSave != null )
      {
         // Start saving
         typeKeys = currentResMod.getTypes();
         while( typeKeys.hasMoreElements() )
         {
            mytype = (String)typeKeys.nextElement();
            if(htab.canHandleType(mytype))
               try
               {
                  saveHandler =
                     (MacResourceHandler)htab.getHandler(mytype).newInstance();
               } catch (Exception e) {
                  System.err.println(e);
               }
            else saveHandler = new DefaultResourceHandler();

            typedir = new File(dirToSave, mytype);
            if(typedir.exists() && !typedir.isDirectory()) typedir.delete();
            if(!typedir.exists()) typedir.mkdir();

            saveHandler.setResData(currentResMod.getResourceType(mytype));
            saveHandler.setResModel(currentResMod);
            saveHandler.init();
            saveHandler.save( typedir );
         }
      }
   }

   /*----------------------------------------------------------------------*/
   void saveHandled( )
   {
      File dirToSave, typedir;
      MacResourceHandler saveHandler = null;
      String mytype;
      Enumeration typeKeys;

      dirToSave = myfp.getSaveDir("Save Handled Types",
                                  currentResMod.getFilename() + "_export");

      if( dirToSave != null )
      {
         // Start saving
         typeKeys = currentResMod.getTypes();
         while( typeKeys.hasMoreElements() )
         {
            mytype = (String)typeKeys.nextElement();
            if(htab.canHandleType(mytype))
            {
               try
               {
                  saveHandler = (MacResourceHandler)htab.
                     getHandler(mytype).newInstance();
               } catch (Exception e) { System.err.println(e); }

               typedir = new File(dirToSave, mytype);
               if(typedir.exists() && !typedir.isDirectory()) typedir.delete();
               if(!typedir.exists()) typedir.mkdir();

               saveHandler.setResData( currentResMod.getResourceType(mytype) );
               saveHandler.setResModel( currentResMod );
               saveHandler.init();
               saveHandler.save( typedir );
            }
         }
      }
   }

   /*----------------------------------------------------------------------*/
   void save(String whichType )
   {
      File typedir, dirToSave;
      MacResourceHandler saveHandler = null;

      if(whichType == null) 
      {
         System.err.println("ERROR: no Type Selected");
         return;
      }

      // Be sure there is something to save
      if(currentResMod == null)
      {
         System.err.println("ERROR: no Resource Model Available");
         return;
      }

      dirToSave = myfp.getSaveDir("Save Current Type",
                                  currentResMod.getFilename() + "_export");
      if( dirToSave != null )
      {
         // Start saving
         if(htab.canHandleType(whichType))
            try
            {
               saveHandler =
                  (MacResourceHandler)htab.getHandler(whichType).newInstance();
            } catch (Exception e) { System.err.println(e); }
         else
            saveHandler = new DefaultResourceHandler();
      
         typedir = new File(dirToSave, whichType);
         if(typedir.exists() && !typedir.isDirectory()) typedir.delete();
         if(!typedir.exists()) typedir.mkdir();

         saveHandler.setResData(currentResMod.getResourceType(whichType));
         saveHandler.setResModel( currentResMod );
         saveHandler.init();
         saveHandler.save( typedir );
      }
   }

   /*----------------------------------------------------------------------*/
   boolean isLoaded( File fileToCheck )
   {
      // Not implemented yet
      Enumeration docKeys = resModTable.keys();
      ResourceModel tmpResMod;

      while(docKeys.hasMoreElements())
      {
         tmpResMod = (ResourceModel) resModTable.get(docKeys.nextElement());
         if(fileToCheck.getAbsolutePath().compareTo(tmpResMod.getFilename()) == 0)
            return true;
      }

      return false;
   }

   /*----------------------------------------------------------------------*/
   void load( )
   {
      File fileToOpen = myfp.getFileToOpen();
      if(fileToOpen != null) load( fileToOpen );
   }

   /*----------------------------------------------------------------------*/
   void load( File inFile )
   {
      RandomAccessFile tmpRAFile;
      MacBinaryHeader mbh;

      if(!inFile.exists()) { myfp.tellFileMissing(inFile.getName()); return; }
      if(!inFile.isFile()) { myfp.tellNotFile(inFile.getName());     return; }
      if(isLoaded(inFile)) { myfp.tellFileLoaded(inFile.getName());  return; }

      // File exists - open and load
      try
      {
         ResourceModel tmpResMod = new ResourceModel();

         tmpRAFile = new RandomAccessFile(inFile, "r");
         tmpResMod.init();
         tmpResMod.setFilename(inFile.getPath());

         // Check to see if this is a MacBinary file
         mbh = new MacBinaryHeader();
         mbh.read(tmpRAFile);
         if(mbh.validate())
         {
            // This is a MacBinary file - must always seek to ResFork
            tmpRAFile.seek(mbh.getResForkOffset());
            tmpResMod.read(tmpRAFile, mbh.getResForkOffset());
         } else {
            // ASSUME an extracted Resource Fork - must always seek to top
            tmpRAFile.seek(0);
            tmpResMod.read(tmpRAFile);
         }

         tmpRAFile.close();
         currentResMod = tmpResMod;
         insert(inFile);

         setChanged();
         notifyObservers();
      } catch(Exception ioe) { myfp.tellCannotOpen( inFile.getName() ); }

      tmpRAFile = null;
   }

   /*----------------------------------------------------------------------*/
   void insert( File inFile )
   {
      String filename = inFile.getName();
      int n = 2;

      while(resModTable.containsKey(filename))
         filename = inFile.getName() + " <" + n++ + ">";

      currentName = filename;
      resModTable.put( currentName, currentResMod );
   }

   /*----------------------------------------------------------------------*/
   void choose( String choice )
   {
      if(resModTable.containsKey(choice))
      {
         currentName = choice;
         currentResMod = (ResourceModel)resModTable.get(currentName);

         setChanged();
         notifyObservers();
      }
   }

   /*----------------------------------------------------------------------*/
   void close()
   {
      // Seems like this could be easier
      // ------------------------------------------------------------
      // Want to remove current ResourceModel from table and set next
      // one to current or if there is no next, use prev
      //
      Enumeration docKeys = resModTable.keys();
      String tmpKey = null;

      while(docKeys.hasMoreElements())
      {
         Object mykey = docKeys.nextElement();
         if(resModTable.get(mykey) == currentResMod)
         {
            resModTable.remove(mykey);
            if(docKeys.hasMoreElements()) 
               tmpKey = (String)docKeys.nextElement();
            currentName   = tmpKey;
            currentResMod = (ResourceModel)resModTable.get(tmpKey);
         } else tmpKey = (String)mykey;
      }

      setChanged();
      notifyObservers();
   }

   /*--------------------------------------------------------------------*/
   void closeAll()
   {
      Enumeration docKeys = resModTable.keys();
      while(docKeys.hasMoreElements())
         resModTable.remove(docKeys.nextElement());
      currentName   = null;
      currentResMod = null;

      setChanged();
      notifyObservers();
   }

   /*----------------------------------------------------------------------*/
   String[] listDocuments()
   {
      String outDocs[];
      Set docSet;

      docSet  = resModTable.keySet();
      outDocs = new String[docSet.size()];
      docSet.toArray(outDocs);
      Arrays.sort(outDocs);

      return outDocs;
   }
}
