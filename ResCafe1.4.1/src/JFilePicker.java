/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/JFilePicker.java,v 1.4 1999/10/28 04:00:42 gbsmith Exp $ */

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.io.File;

import java.awt.Dialog;
import java.awt.Frame;

/*=======================================================================*/
/*
 * $Log: JFilePicker.java,v $
 * Revision 1.4  1999/10/28 04:00:42  gbsmith
 * Added tellFileLoaded() method.
 *
 * Revision 1.3  1999/10/21 22:49:11  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.2  1999/10/17 20:33:58  gbsmith
 * Added 'tellCannotOpen' method implementation
 *
 * Revision 1.1  1999/10/08 01:46:33  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
class JFilePicker implements FilePicker
{
   /*--- Data -----------------------------------------------------------*/
   JFileChooser fchooser;
   JOptionPane jop;
   Dialog msgdialog;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: JFilePicker.java,v 1.4 1999/10/28 04:00:42 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   JFilePicker()
   {
      fchooser = new JFileChooser();
      jop = new JOptionPane();
   }

   /*--------------------------------------------------------------------*/
   public File getFileToOpen()
   {
      int state = fchooser.showOpenDialog(null);
      File theFile = fchooser.getSelectedFile();

      if(theFile != null && state == JFileChooser.APPROVE_OPTION)
         return theFile;
      else
         return null;
   }
   /*--------------------------------------------------------------------*/
   public File getSaveDir( String chooserTitle, String defaultName )
   {
      // Provides for a preset filename
      fchooser.setSelectedFile( new File(defaultName) );
      return getSaveDir( chooserTitle );
   }

   /*--------------------------------------------------------------------*/
   public File getSaveDir( String chooserTitle )
   {
      int state;
      File theDir;

      // Get name of top directory
      fchooser.setDialogTitle( chooserTitle );
      fchooser.setFileSelectionMode( fchooser.FILES_AND_DIRECTORIES );
      state = fchooser.showSaveDialog( null );
      theDir = fchooser.getSelectedFile();

      if(theDir == null || state != JFileChooser.APPROVE_OPTION)
      {
         System.out.println("Save Resources Cancelled");
         return null;
      }

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
      int value;
      Object message[] =
         new String[]{"The directory \'" + dirname + "\' already exists. ",
                      "Some files inside it may be overwritten. ",
                      "Do you still want to continue?"};

      value = jop.showConfirmDialog(
         null,     // Parent
         message,
         "Directory Already Exists",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.WARNING_MESSAGE
         );

      if (value == JOptionPane.YES_OPTION) return true;
      else return false;
   }

   /*--------------------------------------------------------------------*/
   public boolean askExistingFile( String dirname )
   {
      int value;
      Object message[] =
         new String[]{ "The file \'" + dirname + "\' already exists. ",
                       "Do you want to overwrite it?" };

      value = jop.showConfirmDialog(
         null,     // Parent
         message,
         "File Already Exists",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.WARNING_MESSAGE
         );

      if (value == JOptionPane.YES_OPTION) return true;
      else return false;
   }

   /*--------------------------------------------------------------------*/
   public void tellNotFile( String filename )
   {
      String message =  new String("\'" + filename + "\' exists, " +
                                   "but is not a file!");

      jop.showMessageDialog(
         null,     // Parent
         message,
         "Not a Valid File",
         JOptionPane.ERROR_MESSAGE
         );
   }

   /*--------------------------------------------------------------------*/
   public void tellCannotOpen( String filename )
   {
      String message =  new String("Cannot open or cannot read \'"
                                   + filename + "\'. Sorry.");

      jop.showMessageDialog(
         null,     // Parent
         message,
         "File Read Error",
         JOptionPane.ERROR_MESSAGE
         );
   }

   /*--------------------------------------------------------------------*/
   public void tellFileMissing( String filename )
   {
      String message =  new String("The file \'" + filename +
                                   "\' does not exist!");

      jop.showMessageDialog(
         null,     // Parent
         message,
         "File Does Not Exist",
         JOptionPane.ERROR_MESSAGE
         );
   }

   /*--------------------------------------------------------------------*/
   public void tellFileLoaded( String filename )
   {
      String message =  new String("The file \'" + filename +
                                   "\' is already loaded!");

      jop.showMessageDialog(
         null,     // Parent
         message,
         "File Already Loaded",
         JOptionPane.ERROR_MESSAGE
         );
   }
}
