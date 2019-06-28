/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/AWTFilePicker.java,v 1.2 1999/10/21 20:34:42 gbsmith Exp $ */

import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;

import java.io.File;

/*====================================================================*/
/*
 * $Log: AWTFilePicker.java,v $
 * Revision 1.2  1999/10/21 20:34:42  gbsmith
 * Added Copyright notice
 *
 * Revision 1.1  1999/10/08 01:35:34  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/ 

/*====================================================================*/
// The old style AWT version that (sorta) does the same thing as
// JFilePicker
class AWTFilePicker implements FilePicker
{
   /*--- Data --------------------------------------------------------*/
   Dialog msgdialog;
   FileDialog fdialog;
   Frame foster;

   /*------ RCS ------------------------------------------------------*/
   static final String rcsid = "$Id: AWTFilePicker.java,v 1.2 1999/10/21 20:34:42 gbsmith Exp $";

   /*--- Methods -----------------------------------------------------*/
   AWTFilePicker()
   {
      //fchooser = new JFileChooser();
      //jop = new JOptionPane();
      foster = new Frame();
   }

   /*-----------------------------------------------------------------*/
   public File getFileToOpen()
   {
      fdialog = new FileDialog(foster, "Load A Resource File",
                               FileDialog.LOAD);
      fdialog.show();

      if(fdialog.getDirectory() == null ||
         fdialog.getFile()      == null )
      {
         System.out.println("FileDialog Cancelled");
         return null;
      }

      return new File(fdialog.getDirectory() + fdialog.getFile());
   }

   /*-----------------------------------------------------------------*/
   public File getSaveDir( String chooserTitle, String defaultName )
   {
      File theDir;

      // Get name of top directory
      fdialog = new FileDialog(foster, "Save All Resources",
                               FileDialog.SAVE);
      fdialog.setFile( defaultName );
      fdialog.show();

      // Cancel if nothing...
      if(fdialog.getDirectory() == null ||
         fdialog.getFile()      == null )
      {
         System.out.println("Save Resources Cancelled");
         return null;
      }

      theDir = new File(fdialog.getDirectory() + fdialog.getFile());

      // Check if name exists as dir or file
      if(theDir.exists())
      {
         if(theDir.isDirectory())
         {
            if(!askExistingDir( theDir.getName() ))
            {
               System.out.println("ABORTED.");
               return null;
            }
         } else {
            if(askExistingFile( theDir.getName() ))
            {
               theDir.delete(); // Replace existing file
               theDir.mkdir();  // with a directory
            } else {
               System.out.println("ABORTED.");
               return null;
            }
         }
      } else {
         theDir.mkdir();
      }

      return theDir;
   }

   /*-----------------------------------------------------------------*/
   public File getSaveDir( String chooserTitle )
   {
      File theDir;

      // Get name of top directory
      fdialog = new FileDialog(foster, "Save All Resources",
                               FileDialog.SAVE);
      fdialog.show();

      // Cancel if nothing...
      if(fdialog.getDirectory() == null ||
         fdialog.getFile()      == null )
      {
         System.out.println("Save Resources Cancelled");
         return null;
      }

      theDir = new File(fdialog.getDirectory() + fdialog.getFile());

      // Check if name exists as dir or file
      if(theDir.exists())
      {
         if(theDir.isDirectory())
         {
            if(!askExistingDir( theDir.getName() ))
            {
               System.out.println("ABORTED.");
               return null;
            }
         } else {
            if(askExistingFile( theDir.getName() ))
            {
               theDir.delete(); // Replace existing file
               theDir.mkdir();  // with a directory
            } else {
               System.out.println("ABORTED.");
               return null;
            }
         }
      } else {
         theDir.mkdir();
      }

      return theDir;
   }

   /*--------------------------------------------------------------------*/
   public boolean askExistingDir( String dirname )
   {
      msgdialog = new FileExistsDialog(foster, dirname);
      msgdialog.show();

      if(!((FileExistsDialog)msgdialog).getOverwrite())
      {
         System.out.println("ABORTED.");
         return false;
      }

      return true;
   }

   /*--------------------------------------------------------------------*/
   public boolean askExistingFile( String dirname )
   {
      msgdialog = new FileExistsDialog(foster, dirname);
      msgdialog.show();

      if(!((FileExistsDialog)msgdialog).getOverwrite())
      {
         System.out.println("ABORTED.");
         return false;
      }

      return true;
   }

   /*--------------------------------------------------------------------*/
   public void tellNotFile( String filename )
   {
      msgdialog = new MessageDialog (new Frame(), filename +
                                     " exists but is not a file.",
                                     "File Error");
      msgdialog.show();
   }

   /*--------------------------------------------------------------------*/
   public void tellFileMissing( String filename )
   {
      msgdialog = new MessageDialog (new Frame(), filename +
                                     " does not exist!",
                                     "File Error");
      msgdialog.show();
   }
}
