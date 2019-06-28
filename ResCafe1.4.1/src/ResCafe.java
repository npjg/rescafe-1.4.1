/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/ResCafe.java,v 1.10 2000/12/11 19:18:42 gbsmith Exp $ */

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

import java.io.File;
import java.io.RandomAccessFile;

import ResourceManager.*;

// ResCafé
/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: ResCafe.java,v $
 * Revision 1.10  2000/12/11 19:18:42  gbsmith
 * Switched to SplashScreen that loads a default image from the JAR file
 *
 * Revision 1.9  2000/11/27 19:37:19  gbsmith
 * Added new splash screen which tracks loading. Now uses Unicode for e-aigu.
 * Incremented version to 1.3.
 *
 * Revision 1.8  2000/05/25 06:39:05  gbsmith
 * Can now load multiple files given as arguments rather than just
 * one. Also added version String for title (!=RCS version).
 *
 * Revision 1.7  2000/05/24 06:55:30  gbsmith
 * New handlerView feature introduced.
 *
 * Revision 1.6  1999/10/28 04:01:33  gbsmith
 * Changed to support DocumentManager vs. one ResourceModel.
 *
 * Revision 1.5  1999/10/27 07:18:31  gbsmith
 * Put handler loading into a thread
 *
 * Revision 1.4  1999/10/21 23:08:54  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.3  1999/10/19 03:56:30  gbsmith
 * Added accented e to title. Moved window slightly.
 *
 * Revision 1.2  1999/10/13 07:56:26  gbsmith
 * Altered load call to pass a 'File' rather than a
 * 'String' filename
 *
 * Revision 1.1  1999/10/04 21:24:02  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
public class ResCafe
{
   /*--- Data -----------------------------------------------------------*/
   jMainResourceView myview; // Sort of the (GUI) heart of the system
   jHandlerView      hview;
   DocumentManager   mydocmgr;
   HandlerTable      myhandlers;
   FileController    myfctrl;
   RCSplashWindow    myrcsw;

   static String version = "1.3";
   
   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: ResCafe.java,v 1.10 2000/12/11 19:18:42 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public static void main( String args[] )
   {
      ResCafe app = new ResCafe();

      // Load files if given a names on the command-line
      if(args.length > 0)
      {
         app.myrcsw.updateText("Loading Resource Files...");
         for(int a = 0; a < args.length; a++) 
            app.mydocmgr.load(new File(args[a]));
      }

      if(args.length > 0) app.myrcsw.updateText("Files loaded");

      app.go();
   }

   /*--------------------------------------------------------------------*/
   public ResCafe()
   {
      myview     = new jMainResourceView("ResCaf\u00e9 " + version +
                                         " Resource Extractor");
      myrcsw     = new RCSplashWindow(myview);
      mydocmgr   = new DocumentManager();
      myfctrl    = new FileController();
      myhandlers = new HandlerTable();

      // Load handlers in own thread
      myrcsw.updateText("Loading Handlers...");
      myhandlers.addObserver(myrcsw);
      hview = jHandlerView.getInstance();
      hview.setHandlerModel(myhandlers);
      hview.setSize( 400, 300 );
      hview.show();

      //Thread handlerThread = new Thread(myhandlers);
      //handlerThread.start();
      myhandlers.build();
      
      // Assemble view and attach parts
      myrcsw.updateText("Assembling GUI...");
      myview.assemble();

      myrcsw.updateText("Making MVC connections...");
      myview.setDocManager(mydocmgr);
      myview.setHandlers(myhandlers);
      myview.setFileController(myfctrl);

      // Attach parts to Controller
      myfctrl.setView(myview);
      myfctrl.setDocManager(mydocmgr);

      // Attach parts to Doc Manager
      mydocmgr.setHandlers(myhandlers);
   }

   /*--------------------------------------------------------------------*/
   public void go()
   {
      // Show the view... and begin
      myview.setLocation( 75, 75 );

      myview.setSize( 900, 550 );
      myview.show();
      myhandlers.deleteObserver(myrcsw);
      myrcsw.close();
   }   
}
