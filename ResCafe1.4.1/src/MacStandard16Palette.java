/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/MacStandard16Palette.java,v 1.6 1999/12/19 01:28:57 gbsmith Exp $ */

import java.awt.image.IndexColorModel;

/*=======================================================================*/
/*
 * $Log: MacStandard16Palette.java,v $
 * Revision 1.6  1999/12/19 01:28:57  gbsmith
 * Added an additional transparent entry in hopes of applying
 * the mask bitmap to make XPMs with transparency.
 *
 * Revision 1.5  1999/10/21 23:05:40  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.4  1999/10/17 23:01:27  gbsmith
 * Added 'getColorModel' method that will construct an IndexColorModel
 * for the user
 *
 * Revision 1.3  1999/10/13 07:54:47  gbsmith
 * Made class public
 *
 * Revision 1.2  1999/10/04 21:20:57  gbsmith
 * Added some comments.
 *
 * Revision 1.1  1999/09/30 05:25:20  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class MacStandard16Palette
{
   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: MacStandard16Palette.java,v 1.6 1999/12/19 01:28:57 gbsmith Exp $";

   /*--- Data -----------------------------------------------------------*/
   static private final byte reds[] =
   {  -1,  -4,  -1,  -35,  -14,   70,   0,   2,
      31,   0,  86, -112,  -64, -128,  64,   0,
      0 };

   static private final byte greens[] =
   {  -1, -13, 100,    8,    8,    0,   0, -85,
     -73, 100,  44,  113,  -64, -128,  64,   0,
       0 };

   static private final byte blues[] =
   {  -1,   5,   2,    6, -124,  -91, -44, -22,
      20,  17,   5,   58,  -64, -128,  64,   0,
       0 };

   static private final int alpha = 16;

   /*--- Methods --------------------------------------------------------*/
   public static byte[] getReds()   { return reds; }
   public static byte[] getGreens() { return greens; }
   public static byte[] getBlues()  { return blues; }

   public static IndexColorModel getColorModel()
   {
      return new IndexColorModel(8, 17, reds, greens, blues, alpha );
   }
}
