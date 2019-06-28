/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/ThreadedResourceSaver.java,v 1.2 2000/05/24 06:56:29 gbsmith Exp $ */

import java.io.File;

import java.util.Enumeration;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: ThreadedResourceSaver.java,v $
 * Revision 1.2  2000/05/24 06:56:29  gbsmith
 * Just some cosmetic code formatting changes...
 *
 * Revision 1.1  1999/10/28 06:18:12  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
class ThreadedResourceSaver extends Thread implements Runnable
{
   /*--- Data -----------------------------------------------------------*/
   static final int CURRENT = 0;
   static final int HANDLED = 1;
   static final int ALL     = 2;

   int mode = 0;
   HandlerTable htab       = null;
   ResourceModel resToSave = null;
   File outDir             = null;
   String currentType      = null;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: ThreadedResourceSaver.java,v 1.2 2000/05/24 06:56:29 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   ThreadedResourceSaver(int inmode)
   {
      super();

      if(inmode > ALL) mode = ALL;
      else if(inmode < CURRENT) mode = CURRENT;
      else mode = inmode;
   }

   /*--------------------------------------------------------------------*/
   void setHandlers(HandlerTable inhtab)
   {
      htab = inhtab;
   }

   /*--------------------------------------------------------------------*/
   void setModel(ResourceModel inresmod)
   {
      resToSave = inresmod;
   }

   /*--------------------------------------------------------------------*/
   void setDir(File savedir)
   {
      outDir = savedir;
   }

   /*--------------------------------------------------------------------*/
   void setType(String inType) // Only necessary for CURRENT mode
   {
      currentType = inType;
   }

   /*--------------------------------------------------------------------*/
   public void run()
   {
      if(htab == null)      return;
      if(resToSave == null) return;
      if(outDir == null)    return;

      if(mode == ALL)     saveAll();
      if(mode == HANDLED) saveHandled();
      if(mode == CURRENT)
      {
         if(currentType == null) return;
         saveCurrent();
      }
   }

   /*--------------------------------------------------------------------*/
   void saveAll()
   {
      File typedir;
      Enumeration typeKeys;
      MacResourceHandler saveHandler = null;

      // Start saving
      typeKeys = resToSave.getTypes();
      while( typeKeys.hasMoreElements() )
      {
         currentType = (String)typeKeys.nextElement();

         if(htab.canHandleType(currentType))
            try
            {
               saveHandler =
                  (MacResourceHandler)htab.getHandler(currentType).newInstance();
            } catch (Exception e) {
               System.err.println(e);
            }
         else
            saveHandler = new DefaultResourceHandler();


         typedir = new File(outDir, currentType);
         if(typedir.exists() && !typedir.isDirectory()) typedir.delete();
         if(!typedir.exists()) typedir.mkdir();

         saveHandler.setResData(resToSave.getResourceType(currentType));
         saveHandler.setResModel(resToSave);
         saveHandler.init();
         saveHandler.save( typedir );
      }
   }

   /*--------------------------------------------------------------------*/
   void saveHandled()
   {
      File typedir;
      MacResourceHandler saveHandler = null;
      Enumeration typeKeys;

      // Start saving
      typeKeys = resToSave.getTypes();
      while( typeKeys.hasMoreElements() )
      {
         currentType = (String)typeKeys.nextElement();
         if(htab.canHandleType(currentType))
         {
            try
            {
               saveHandler = (MacResourceHandler)htab.
                  getHandler(currentType).newInstance();
            } catch (Exception e) {
               System.err.println(e);
            }

            typedir = new File(outDir, currentType);
            if(typedir.exists() && !typedir.isDirectory()) typedir.delete();
            if(!typedir.exists()) typedir.mkdir();

            saveHandler.setResData( resToSave.getResourceType(currentType) );
            saveHandler.setResModel( resToSave );
            saveHandler.init();
            saveHandler.save( typedir );
         }
      }
   }

   /*--------------------------------------------------------------------*/
   void saveCurrent()
   {
      File typedir;
      MacResourceHandler saveHandler = null;

      // Start saving
      if(htab.canHandleType(currentType))
      {
         try
         {
            saveHandler =
               (MacResourceHandler)htab.getHandler(currentType).newInstance();
         } catch (Exception e) {
            System.err.println(e);
         }
      } else {
         saveHandler = new DefaultResourceHandler();
      }

      typedir = new File(outDir, currentType);
      if(typedir.exists() && !typedir.isDirectory()) typedir.delete();
      if(!typedir.exists()) typedir.mkdir();

      saveHandler.setResData(resToSave.getResourceType(currentType));
      saveHandler.setResModel( resToSave );
      saveHandler.init();
      saveHandler.save( typedir );
   }
}
