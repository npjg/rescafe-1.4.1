/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/FileController.java,v 1.7 1999/10/28 03:59:54 gbsmith Exp $ */

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Frame;
import java.io.File;
import java.util.Enumeration;

/*====================================================================*/
/*
 * $Log: FileController.java,v $
 * Revision 1.7  1999/10/28 03:59:54  gbsmith
 * Moved actual File I/O stuff into DocumentManager class.
 *
 * Revision 1.6  1999/10/21 22:38:58  gbsmith
 * Added Copyright notice. Added a few comments.
 *
 * Revision 1.5  1999/10/17 20:37:05  gbsmith
 * Added error handling stuff for invalid regular files:
 *   - Display a dialog upon failure
 *   - Do loading with a temp ResourceModel so as to avoid
 *     overwriting existing model data if load fails
 *
 * Revision 1.4  1999/10/13 07:10:32  gbsmith
 * Added MacBinaryHeader check
 *
 * Revision 1.3  1999/10/08 03:16:58  gbsmith
 * Separated User-interaction functionality (i.e. GUI stuff) into FilePicker
 * and subclasses - this makes the Controller more independent and allows for
 * possible redesign or reuse.
 *
 * Revision 1.2  1999/10/04 21:10:40  gbsmith
 * Made class imports more explicit. Added Resource save functionality.
 *
 * Revision 1.1  1999/09/30 05:19:34  gbsmith
 * Initial revision
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*====================================================================*/
class FileController implements ActionListener
{
   /*--- Data --------------------------------------------------------*/
   DocumentManager docmgr;
   String currentType; // Should be set by the view
   Frame mrview; // Does this even need to be here? Should it be Object?

   File tmpFile;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: FileController.java,v 1.7 1999/10/28 03:59:54 gbsmith Exp $";

   /*--- Methods -----------------------------------------------------*/
   public FileController()
   {
      docmgr = null;
      mrview = null;
      currentType = null;
      //myfp = new AWTFilePicker();
   }

   /*-----------------------------------------------------------------*/
   public void setDocManager( DocumentManager indocmgr )
   {
      docmgr = indocmgr;
      currentType = null; // Reset this
   }

   /*-----------------------------------------------------------------*/
   public void setView(Frame inview)
   {
      mrview = inview;
      currentType = null; // Reset this
   }

   /*-----------------------------------------------------------------*/
   public void setCurrentType( String newtype )
   {
      // This is still kinda kludgy
      currentType = newtype;
   }

   /*-----------------------------------------------------------------*/
   public void actionPerformed( ActionEvent ae )
   {
      String command = ae.getActionCommand();
      if(docmgr == null)
         System.err.println("ERROR: no Doc Manager available\n");
      else
      {
         if(command.equals("Open..."))              docmgr.load();
         else if(command.equals("Save All..."))     docmgr.saveAll();
         else if(command.equals("Save Handled...")) docmgr.saveHandled();
         else if(command.equals("Save Current...")) docmgr.save(currentType);
         else if(command.equals("Close"))           docmgr.close();
         else if(command.equals("Close All"))       docmgr.closeAll();
         else System.err.println("ERROR: invalid menu item\n");         
      }
   }
}
