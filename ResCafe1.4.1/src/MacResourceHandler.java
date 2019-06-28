/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/MacResourceHandler.java,v 1.6 2000/05/24 06:23:46 gbsmith Exp $ */

import javax.swing.JPanel;

import java.io.File;
import java.io.FileOutputStream;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: MacResourceHandler.java,v $
 * Revision 1.6  2000/05/24 06:23:46  gbsmith
 * Changed up the save method to take care of errant file separator
 * chars that could get inserted into the save location form the
 * resource name, causing file writing errors.
 *
 * Revision 1.5  1999/10/21 23:01:38  gbsmith
 * Added explicit JPanel class import.
 *
 * Revision 1.4  1999/10/19 05:40:25  gbsmith
 * Added skeletal (but not abstract) 'about' method for getting
 * info about the handler class. Also added Copyright.
 *
 * Revision 1.3  1999/10/13 23:32:01  gbsmith
 * Rearranged some methods. Made some data protected so it can be seen by
 * subclasses outside of the package (mainly from the alt class loaders).
 * Moved in the default raw bytes save method from the old default handler.
 *
 * Revision 1.2  1999/10/04 21:20:20  gbsmith
 * Redesigned type signature and initialization. Added ResourceModel
 * method and save method.
 *
 * Revision 1.1  1999/09/30 05:23:43  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public abstract class MacResourceHandler extends JPanel
{
   /*--- Data -----------------------------------------------------------*/
   //String types[];
   static protected String author;
   static protected String version;

   protected ResourceModel resMod;  // Needs 'protected' so extra-package
   protected ResourceType  resData; // classes can access

   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: MacResourceHandler.java,v 1.6 2000/05/24 06:23:46 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   /**
    * Permanent accessor method used to set ResourceType that
    * class will be processing
    * @param inResData Resources of type that class will handle
    */
   public final void setResData(ResourceType inResData)
   {
      resData = inResData;
   }

   /*--------------------------------------------------------------------*/
   /**
    * Permanent accessor method used to give class access to
    * Resources of additional types.
    * @param inResMod Set of all Resources
    */
   public final void setResModel( ResourceModel inResMod )
   {
      resMod = inResMod;
   }

   /*--------------------------------------------------------------------*/
   public void save ( File savedir)
   {
      StringBuffer tmpfilename;
      String filename, saveType;
      Resource myResArray[];

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      saveType   = resData.getID();
      myResArray = resData.getResArray();

      System.out.println("Saving resources of type \'" +
                         saveType + "\' as bytes");
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer(savedir.getPath());
         tmpfilename.append( File.separator + myResArray[i].getID() );
         if(myResArray[i].getName() != null)
            tmpfilename.append("_" + myResArray[i].getName().
                               replace(' ', '_').
                               replace(File.separatorChar, '+'));
         tmpfilename.append(".raw");
         filename = tmpfilename.toString().replace(' ', '_');

         try
         {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(myResArray[i].getData());
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }


   /*--------------------------------------------------------------------*/
   public String[] about()
   {
      return null;
   }

   /*--------------------------------------------------------------------*/
   public abstract String[] getTypes( );
   public abstract void init( );
   public abstract void display( );
}
