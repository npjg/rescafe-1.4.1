/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/cicnParser.java,v 1.3 1999/10/21 22:29:30 gbsmith Exp $ */
import java.awt.Panel;
import java.awt.Image;

import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

/*=========================================================================*/
/*
 * $Log: cicnParser.java,v $
 * Revision 1.3  1999/10/21 22:29:30  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.2  1999/10/17 23:05:24  gbsmith
 * Seemed to have fixed pixel extraction (which bits represent which pixels)
 * and color mapping problems (sometimes cicn Colors are given out
 * of sequence). Also started using new getColorModel method on standard
 * palettes.
 *
 * Revision 1.1  1999/10/17 21:26:28  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=========================================================================*/
public class cicnParser
{
   /*--- Data -------------------------------------------------------------*/
   Image mask, bitmap, icon; // Processed images

   // Icon's pixel map
   private long baseAddr;   // 4 bytes - should be 0
   private int iconRowBytes;    // 2 bytes - there is problem here...
   private int iconBounds[] = new int[4]; // 8 bytes
   private int pmVersion;   // 2 bytes - should be 0
   private int packType;    // 2 bytes - should be 0
   private int packSize;    // 4 bytes - should be 0
   private int hRes;        // 4 bytes - should be 72
   private int vRes;        // 4 bytes - should be 72
   private int pixelType;   // 2 bytes - should be 0
   private int pixelSize;   // 2 bytes
   private int cmpCount;    // 2 bytes - should be 1
   private int cmpSize;     // 2 bytes
   private long planeByte;  // 4 bytes - should be 0
   private long pmTable;    // 4 bytes - should be 0
   private long pmReserved; // 4 bytes - should be 0

   // Mask bitmap
   private long maskBaseAddr; // 4 bytes - should be 0
   private int maskRowBytes;  // 2 bytes
   private int maskBounds[] = new int[4]; // 8 bytes

   // Icon bitmap
   private long bitmapBaseAddr; // 4 bytes - should be 0
   private int bitmapRowBytes;  // 2 bytes
   private int bitmapBounds[] = new int[4]; // 8 bytes

   private long iconDataHandle; // 4 bytes - should be 0

   // Actual image data here
   byte iconData[]; // The color icon data
   byte maskData[];
   byte bitmapData[];

   // Color Table Stuff
   private long ctSeed;
   private int ctFlag;
   private int ctSize;

   private int colorID[]; // Should probably
   private byte reds[];    // replace this
   private byte greens[];  // with a better
   private byte blues[];   // data structure

   private int colorRemap[];   

   /*----- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: cicnParser.java,v 1.3 1999/10/21 22:29:30 gbsmith Exp $";

   /*--- Methods ----------------------------------------------------------*/
   public cicnParser()
   {
   }

   /*----------------------------------------------------------------------*/
   private void reset()
   {
      // Clean out every thing...
      // Images
      mask    = null;
      bitmap  = null;
      icon    = null;

      // Colors
      colorID = null;
      reds    = null;
      greens  = null;
      blues   = null;

      colorRemap = null;
   }


   /*----------------------------------------------------------------------*/
   public static void main( String args[] )
   {
      FileInputStream fis;
      cicnParser mycp;
      if(args.length <= 0)
      {
         System.err.println("ERROR: No args given");
         System.exit(1);
      }

      mycp = new cicnParser();

      try
      {
         fis = new FileInputStream(args[0]);
         mycp.read(fis);
         mycp.print(System.out);
         mycp.getIcon();
      } catch(Exception ioe) { ioe.printStackTrace(); }

      System.exit(0);
   }

   /*----------------------------------------------------------------------*/
   public void read(InputStream inis) throws IOException
   {
      reset();

      DataInputStream dis = new DataInputStream(inis);

      // Start processing...
      baseAddr = dis.readInt();
      //iconRowBytes = dis.readUnsignedShort();
      // Keep only lowbyte for now - until I figure out what to do with
      // some weirdness in the hi byte
      dis.readUnsignedByte();
      iconRowBytes = dis.readUnsignedByte();

      // Should probably change these names
      iconBounds[0] = dis.readUnsignedShort();
      iconBounds[1] = dis.readUnsignedShort();
      iconBounds[2] = dis.readUnsignedShort();
      iconBounds[3] = dis.readUnsignedShort();

      pmVersion = dis.readUnsignedShort();
      packType  = dis.readUnsignedShort();
      packSize  = dis.readUnsignedShort(); // This was supposed to be 4 bytes

      hRes = dis.readInt();
      vRes = dis.readInt();

      dis.readUnsignedShort(); // dump 2 bytes - why did this happen?

      pixelType = dis.readUnsignedShort();
      pixelSize = dis.readUnsignedShort();
      cmpCount  = dis.readUnsignedShort();
      cmpSize   = dis.readUnsignedShort();

      planeByte  = dis.readInt();
      pmTable    = dis.readInt();
      pmReserved = dis.readInt();

      // Mask bitmap
      maskBaseAddr = dis.readInt();
      maskRowBytes = dis.readUnsignedShort();
      maskBounds[0] = dis.readUnsignedShort();
      maskBounds[1] = dis.readUnsignedShort();
      maskBounds[2] = dis.readUnsignedShort();
      maskBounds[3] = dis.readUnsignedShort();

      // Icon bitmap
      bitmapBaseAddr = dis.readInt();
      bitmapRowBytes = dis.readUnsignedShort();
      bitmapBounds[0] = dis.readUnsignedShort();
      bitmapBounds[1] = dis.readUnsignedShort();
      bitmapBounds[2] = dis.readUnsignedShort();
      bitmapBounds[3] = dis.readUnsignedShort();

      // Icon Data Handle
      iconDataHandle = dis.readInt();
      // Mask bytes
      maskData = new byte[maskRowBytes * maskBounds[3]];
      dis.readFully(maskData);

      // Icon bitmap bytes
      bitmapData = new byte[bitmapRowBytes * bitmapBounds[3]];
      dis.readFully(bitmapData);

      // Color table stuff
      ctSeed = dis.readInt();
      ctFlag = dis.readUnsignedShort();
      ctSize = dis.readUnsignedShort() + 1;

      /*
      colorID = new int[ctSize];
      reds    = new byte[ctSize*2];
      greens  = new byte[ctSize*2];
      blues   = new byte[ctSize*2];

      for(int c = 0; c < ctSize; c++)
      {
         colorID[c]     = dis.readUnsignedShort();
         reds[c*2]      = dis.readByte();
         reds[c*2+1]    = dis.readByte();
         greens[c*2]    = dis.readByte();
         greens[c*2+1]  = dis.readByte();
         blues[c*2]     = dis.readByte();
         blues[c*2+1]   = dis.readByte();
      }
      */

      // The colors are 2-byte quantities but we seem to be able to get away with
      // just one of the bytes so far - 16 bit color models have been problematic
      colorID = new int[ctSize];

      reds    = new byte[ctSize];
      greens  = new byte[ctSize];
      blues   = new byte[ctSize];

      int maxColorID = 0;

      for(int c = 0; c < ctSize; c++)
      {
         colorID[c] = dis.readUnsignedShort();
         dis.readByte(); reds[c]    = dis.readByte();
         dis.readByte(); greens[c]  = dis.readByte();
         dis.readByte(); blues[c]   = dis.readByte();

         // Do color remapping to fix non-contiguous indices
         if(maxColorID < colorID[c]) maxColorID = colorID[c];
      }

      // Do color remapping to fix non-contiguous indices
      colorRemap = new int[maxColorID + 1];
      for(int c = 0; c < ctSize; c++)
      {
         colorRemap[colorID[c]] = c; 
         /*
         System.out.println("colorID[" + c + "] = " + colorID[c] +
                            " -> c = " + c);
         */
      }

      // Color pixel bytes - for now
      iconData = new byte[iconRowBytes * iconBounds[3]];
      dis.readFully(iconData );
   }

   /*----------------------------------------------------------------------*/
   public void print(PrintStream ps)
   {
      System.out.println("---------------------------------------------");
      System.out.println("baseAddr = " + baseAddr);
      System.out.println("iconRowBytes = " + iconRowBytes );

      System.out.println("iconBounds = " + iconBounds[0] + ", " + iconBounds[1] +
                         ", " +        iconBounds[2] + ", " + iconBounds[3] );

      System.out.println("pmVersion = " + pmVersion);
      System.out.println("packType  = " + packType);
      System.out.println("packSize  = " + packSize);
      System.out.println("hRes = " + hRes);
      System.out.println("vRes = " + vRes);
      System.out.println("pixelType  = " + pixelType);
      System.out.println("pixelSize  = " + pixelSize);
      System.out.println("cmpCount   = " + cmpCount);
      System.out.println("cmpSize    = " + cmpSize);
      System.out.println("planeByte  = " + planeByte);
      System.out.println("pmTable    = " + pmTable);
      System.out.println("pmReserved = " + pmReserved);
      System.out.println("");

      System.out.println("---------------------------------------------");
      // Mask bitmap
      System.out.println("maskBaseAddr = " + maskBaseAddr);
      System.out.println("maskRowBytes = " + maskRowBytes);
      System.out.println("maskBounds   = " + maskBounds[0] + ", " + maskBounds[1] +
                         ", "              + maskBounds[2] + ", " + maskBounds[3]);
      System.out.println("");

      System.out.println("---------------------------------------------");
      // Icon bitmap
      System.out.println("bitmapBaseAddr = " + bitmapBaseAddr);
      System.out.println("bitmapRowBytes = " + bitmapRowBytes);
      System.out.println("bitmapBounds   = " + bitmapBounds[0] +
                         ", " + bitmapBounds[1] +
                         ", " + bitmapBounds[2] +
                         ", " + bitmapBounds[3]);
      System.out.println("");

      System.out.println("---------------------------------------------");
      System.out.println("Mask Bytes = " +  (maskRowBytes * maskBounds[3]));
      System.out.println("Icon Bytes = " +  (bitmapRowBytes * bitmapBounds[3]));
      System.out.println("");

      System.out.println("---------------------------------------------");
      System.out.println("ctSeed = " + ctSeed);
      System.out.println("ctFlag = " + ctFlag);
      System.out.println("ctSize = " + ctSize);
      System.out.println("");

      for(int c = 0; c < ctSize; c++)
      {
         /*
         System.out.println("Color " + c + "/" + colorID[c] +
                            " = R:" + reds[c*2]   + ", " + reds[c*2+1] +
                            "\t G:" + greens[c*2] + ", " + greens[c*2+1] +
                            "\t B:" + blues[c*2]  + ", " + blues[c*2+1]);
         */
         System.out.println("Color " + c + "/" + colorID[c] +
                            " = R:" + reds[c]   +
                            "\t G:" + greens[c] +
                            "\t B:" + blues[c]);
      }
      System.out.println("");

      System.out.println("---------------------------------------------");
      System.out.println("Pixel Bytes = " +  (iconRowBytes * iconBounds[3]));

   }

   /*----------------------------------------------------------------------*/
   public Image getMask()
   {
      if(mask == null) buildMask();
      return mask;
   }

   /*----------------------------------------------------------------------*/
   private void buildMask()
   {
      if(maskData == null) return; // Nothing to build with...

      int numBytes = (maskBounds[2] - maskBounds[0]) *
         (maskBounds[3] - maskBounds[1]);
      byte expandedData[] = new byte[numBytes];
      int pixelsPerByte = maskBounds[2]/maskRowBytes;

      for ( int j = 0; j < maskData.length; j++)
         for ( int b = 0; b < pixelsPerByte; b++)
            expandedData[j*pixelsPerByte+b] = (byte)((maskData[j] &
                                        (0x80 >>> b)) > 0? 0x0F: 0x00);

      IndexColorModel icm = MacStandard16Palette.getColorModel();
      MemoryImageSource mis =
         new MemoryImageSource(maskBounds[2], maskBounds[3], icm,
                               expandedData, 0, maskBounds[2]);

      mask = (new Panel()).createImage(mis); // This is kinda stupid
   }

   /*----------------------------------------------------------------------*/
   public Image getBitmap()
   {
      if(bitmap == null) buildBitmap();
      return bitmap;
   }

   /*----------------------------------------------------------------------*/
   private void buildBitmap()
   {
      int b, j;
      int numBytes;
      int pixelsPerByte;
      byte expandedData[];

      IndexColorModel icm;
      MemoryImageSource mis;
      
      if(bitmapData == null) return; // Nothing to build with...

      numBytes = (bitmapBounds[2] - bitmapBounds[0]) *
         (bitmapBounds[3] - bitmapBounds[1]);

      icm = MacStandard16Palette.getColorModel();

      expandedData = new byte[ numBytes ];
      pixelsPerByte = bitmapBounds[2]/bitmapRowBytes;

      for ( j = 0; j < bitmapData.length; j++)
         for ( b = 0; b < pixelsPerByte; b++)
            expandedData[j * pixelsPerByte + b] =
               (byte)((bitmapData[j] & (0x80 >>> b)) > 0? 0x0F: 0x00);

      mis = new MemoryImageSource(bitmapBounds[2], bitmapBounds[3], icm,
                                  expandedData, 0, bitmapBounds[2]);

      bitmap = (new Panel()).createImage(mis); // see Icon
   }

   /*----------------------------------------------------------------------*/
   public Image getIcon()
   {
      if(icon == null) buildIcon();
      return icon;
   }

   /*----------------------------------------------------------------------*/
   private void buildIcon()
   {
      int b, j, m, r; 
      int numBytes;
      int pixelsPerByte, bpp;

      byte bitmask;
      byte expandedData[];

      IndexColorModel icm;
      MemoryImageSource mis;
      
      if(iconData == null) return; // Nothing to build with...

      numBytes =
         (iconBounds[2] - iconBounds[0]) *
         (iconBounds[3] - iconBounds[1]);

      icm = new IndexColorModel(8, ctSize, reds, greens, blues);

      //----------------------------------------------------------------
      // Now it gets ugly...
      //----------------------------------------------------------------
      expandedData  = new byte[numBytes];
      pixelsPerByte = iconBounds[2]/iconRowBytes;
      bpp           = 8/pixelsPerByte;

      // build a mask to make sure the pixels are properly shifted out
      bitmask = 0;
      for(m = 0; m < bpp; m++)
      {
         bitmask <<= 1;
         bitmask  |= 1;
      }

      // Extract pixels from bytes
      for ( j = 0; j < iconData.length; j++)
         for ( b = 0; b < pixelsPerByte; b++)
            expandedData[j * pixelsPerByte + (pixelsPerByte - 1 - b)] =
               (byte)( (iconData[j] >>> (b * bpp)) & bitmask );

      // Remap colors
      for( r = 0; r < expandedData.length; r++)
         expandedData[r] = (byte)colorRemap[expandedData[r]];
      
      // And make a Java Image
      mis = new MemoryImageSource(iconBounds[2], iconBounds[3], icm,
                                  expandedData, 0, iconBounds[2]);

      // This is kinda stupid - maybe this method should ask for a
      // component param to use to make the Image
      icon = (new Panel()).createImage(mis);
   }

   /*----------------------------------------------------------------------*/
   /*
   private long get4(int offset)
   {
      long value = 0;
      for(int i = 0; i < 4; i++)
      {
         value <<= 8;
         value |= (info_header[offset + i] & BYTEMASK);
      }

      return value;
   }
   */
   /*----------------------------------------------------------------------*/
}

