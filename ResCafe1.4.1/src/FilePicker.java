/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/FilePicker.java,v 1.4 1999/10/28 04:03:51 gbsmith Exp $ */

import java.io.File;

/*=======================================================================*/
/*
 * $Log: FilePicker.java,v $
 * Revision 1.4  1999/10/28 04:03:51  gbsmith
 * Added tellFileLoaded() method.
 *
 * Revision 1.3  1999/10/21 22:40:12  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.2  1999/10/17 20:33:03  gbsmith
 * Added 'tellCannotOpen' method.
 *
 * Revision 1.1  1999/10/08 01:35:03  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
/**
 * This is the interface for the operations the FileController uses to
 * interact with the user: Requesting filenames, getting overwrite
 * approval, etc. By separating this functionality out, any of several
 * different interfaces(Swing, AWT, even Console based) can be
 * dropped in.
 */
interface FilePicker
{
   File getFileToOpen();
   File getSaveDir( String chooserTitle );
   File getSaveDir( String chooserTitle, String defaultName );

   boolean askExistingDir( String dirname );
   boolean askExistingFile( String dirname );

   void tellNotFile( String filename );
   void tellFileMissing( String filename );
   void tellFileLoaded( String filename );
   void tellCannotOpen( String filename );
}
