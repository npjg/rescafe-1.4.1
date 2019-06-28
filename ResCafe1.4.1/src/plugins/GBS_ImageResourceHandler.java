/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/GBS_ImageResourceHandler.java,v 1.5 2000/11/27 19:39:06 gbsmith Exp $ */

import java.awt.BorderLayout;
import java.awt.Image;

import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import java.io.File;
import java.io.FileWriter;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: GBS_ImageResourceHandler.java,v $
 * Revision 1.5  2000/11/27 19:39:06  gbsmith
 * Reduced 1-bit and 4-bit processing to generic size independent methods
 * allowing for smaller code and easier reuse.
 *
 * Revision 1.4  2000/05/24 06:21:14  gbsmith
 * Now uses custom XpmImage class instead of Jimi for XPM export.
 * Also now subclasses DefaultResourceHandler rather that
 * MacResourceHandler. This allow access to column sorting and
 * optimizing code. Still descends from MacResourceHandler though.
 *
 * Revision 1.3  1999/12/19 05:17:37  gbsmith
 * Moved icon processing/decoding routines into this class from
 * IconFamilyResourceHandler.
 *
 * Revision 1.2  1999/10/21 21:21:16  gbsmith
 * Added copyright notice. Made class imports more explicit.
 *
 * Revision 1.1  1999/10/04 21:58:15  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
public abstract class GBS_ImageResourceHandler extends DefaultResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   protected Image myimages[];

   // Make the color models
   protected IndexColorModel icm16  = MacStandard16Palette.getColorModel();
   protected IndexColorModel icm256 = MacStandard256Palette.getColorModel();

    /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: GBS_ImageResourceHandler.java,v 1.5 2000/11/27 19:39:06 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   protected Image process_1bit( byte rawData[], int dim, boolean doMask )
   {
      MemoryImageSource mis;
      byte iconData[];
      int i, j, b;
      int numbytes, moffset;

      numbytes = dim * dim / 8;  //  _eight_ pixels per byte
      moffset = doMask? numbytes: 0;

      // Grab icon data
      iconData = new byte[dim*dim];
      for ( j = 0; j < numbytes; j++)
         for ( b = 0; b < 8; b++)
            iconData[j*8+b] = (byte)((rawData[j + moffset] &
                                      (0x80 >>> b)) > 0? 0x0F: 0x00);

      mis = new MemoryImageSource(dim, dim, icm16, iconData, 0, dim);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_4bit( byte rawData[], int dim )
   {
      MemoryImageSource mis;
      byte iconData[];

      iconData = new byte[dim * dim];
      for (int j = 0; j < dim*dim/2; j++) // _two_ pixels per byte
      {
         // Grab high 4 bytes
         iconData[j*2]   = (byte)((rawData[j] >> 4) & 0x0F);

         // Grab low 4 bytes
         iconData[j*2+1] = (byte)(rawData[j] & 0x0F);
      }

      mis = new MemoryImageSource(dim, dim, icm16, iconData, 0, dim);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ICN( byte rawData[] )
   {
      return process_1bit( rawData, 32, false );
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ICN_mask( byte rawData[] )
   {
      return process_1bit( rawData, 32, true );
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ics( byte rawData[] )
   {
      return process_1bit( rawData, 16, false );
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ics_mask( byte rawData[] )
   {
      return process_1bit( rawData, 16, true );
   }

   /*--------------------------------------------------------------------*/
   protected Image process_icl4( byte rawData[] )
   {
      return process_4bit( rawData, 32 );
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ics4( byte rawData[] )
   {
      return process_4bit( rawData, 16 );
   }

   /*--------------------------------------------------------------------*/
   protected Image process_icl8( byte rawData[] )
   {
      // Can create icon directly from data
      MemoryImageSource mis =
         new MemoryImageSource(32, 32, icm256, rawData, 0, 32);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ics8( byte rawData[] )
   {
      // Can create icon directly from data
      MemoryImageSource mis =
         new MemoryImageSource(16, 16, icm256, rawData, 0, 16);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   public void save ( File savedir )
   {
      StringBuffer tmpfilename;
      String filename;
      String imgname;
      String saveType;

      XpmImage xpmout;

      File outfile;
      FileWriter fw;

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      Resource myResArray[] = resData.getResArray();
      saveType = resData.getID();

      System.out.println("Saving resources of type \'" + saveType + "\'");
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer( myResArray[i].getID() );
         if(myResArray[i].getName() != null)
         {
            tmpfilename.append("_" + myResArray[i].getName());
            imgname = myResArray[i].getName().replace(' ', '_');
         } else imgname = "untitled";

         tmpfilename.append(".xpm");
         filename = tmpfilename.toString().replace(' ', '_');

         try
         {
            outfile = new File(savedir, filename);
            fw = new FileWriter(outfile);
            xpmout = new XpmImage(imgname, this, myimages[i]);
            xpmout.write(fw);
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
}
