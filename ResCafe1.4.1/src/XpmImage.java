/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/XpmImage.java,v 1.4 2000/05/25 06:17:15 gbsmith Exp $ */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.Writer;

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;

import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/*=======================================================================*/
/* This class was inspired by the xpm-3.4k C package and occasional I    */
/* I have tried to grab some variable names or a small algorithm that    */
/* worked esp well                                                       */
/* Developed by Arnaud Le Hors                                           */
/* Copyright (c) 1989-95 GROUPE BULL                                     */
/*                                                                       */
/* I, however, did write this Java version so                            */
/* Copyright (c) 2000 by G. Brannon Smith -- All Rights Reserved         */
/*                                                                       */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: XpmImage.java,v $
 * Revision 1.4  2000/05/25 06:17:15  gbsmith
 * Added additional getImage() method that uses the Toolkit rather
 * than a Component for Image creation
 *
 * Revision 1.3  2000/05/24 06:05:19  gbsmith
 * Added copyright notice.
 *
 * Revision 1.2  2000/05/24 01:26:29  gbsmith
 * Added support for ColorName lookup
 * Some code cleanup
 *
 * Revision 1.1  2000/05/23 23:35:36  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
public class XpmImage
{
   /*====================================================================*/
   /* Data                                                               */
   /*====================================================================*/
   static String rcsid = "$Id: XpmImage.java,v 1.4 2000/05/25 06:17:15 gbsmith Exp $";

   // the 92 chars we can use to make printable chars
   static String printable = " .XoO+@#$%&*=-;:>,<1234567890qwertyuipasdfghjklzxcvbnmMNBVCZASDFGHJKLPIUYTREWQ!~^/()_`'][{}|";

   // Input stuff
   StreamTokenizer st;

   // Xpm data
   private String name;        /* the name of the created pixmap */
   private int width     =  0; /* the width of the created pixmap */
   private int height    =  0; /* the height of the created pixmap */
   private int x_hotspot = -1; /* the x hotspot's coordinate */
   private int y_hotspot = -1; /* the y hotspot's coordinate */
   private int cpp       =  0; /* Specifies the number of char per pixel */

   // private int nextensions; // part of extension array
   Vector extensions;    /* List of extensions */

   // Comments - these currently don't do much
   private String hints_cmt  = "/* hints */";
   private String colors_cmt = "/* colors */";
   private String pixels_cmt = "/* pixels */";

   // Perhaps these 2 should be merged
   private int ncolors;
   private int[] colors;   /* List of colors */
   private int mask_pixel; /* Color table index of transparent color */

   // This functionality is currently unimplemented. Just stash away here
   private String[] m_colors;  /* List of monochrome       */
   private String[] symbolic;  /* List of symbolic         */
   private String[] g4_colors; /* List of level 4 greys    */
   private String[] g_colors;  /* List of high level greys */

   // Pixel data
   private int[] pixels;

   private boolean hasMask       = false;
   private boolean hasExtensions = false;

   // OUTPUT: used mainly for writing XPM to file
   private String[] index2pchars;    // int -> String

   // INPUT: used mainly for creating XPM from Image or file
   private Hashtable colors2index; // String -> int
   private Hashtable pchars2index; // String -> Integer

   /*====================================================================*/
   /* Methods                                                            */
   /*====================================================================*/

   /*====================================================================*/
   /* CONSTRUCTORS                                                       */
   /*====================================================================*/
   /**
      Create an XPM Object from a stream esp. a file
    */
   public XpmImage(Reader datasrc) throws  IOException
   {
      readXpm(datasrc);
   }

   /*--------------------------------------------------------------------*/
   /**
      Create an XPM Object from a Java Image
   */
   public XpmImage(String iname, Component icomp, Image myimg)
   {
      int[] imgpixels;
      PixelGrabber pg;
      int p, c;

      name = iname;

      // Get the image pixels
      width  = myimg.getWidth(icomp);
      height = myimg.getHeight(icomp);

      imgpixels = new int[width * height];
      pg = new PixelGrabber(myimg, 0, 0, width, height, imgpixels, 0, width);
      try
      {
         pg.grabPixels();
      } catch (InterruptedException e) {
         System.err.println("interrupted waiting for pixels!");
         return;
      }

      // Get list of unique colors using hash
      colors2index = new Hashtable(20); // 20 is kinda arbitrary
      for(p = 0; p < imgpixels.length; p++)
         colors2index.put(new Integer(imgpixels[p]), Boolean.TRUE);

      ncolors = colors2index.size();
      colors = new int[ncolors];

      // Build color map, i.e. hash that associated RGB val with int index
      // And go ahead and see if there is an implicit mask in the image -
      // Check values for a 0x00 alpha and let the last one found be the mask
      c = 0;
      for (Enumeration e = colors2index.keys(); e.hasMoreElements(); c++)
      {
         Integer i = (Integer)e.nextElement();
         colors2index.put(i, new Integer(c));
         colors[c] = i.intValue();
         if( (colors[c] & 0xFF000000) == 0) // Found 0x00 alpha channel
         {
            mask_pixel = c; // NOTE: prev found mask is wiped out
            hasMask    = true;
         }
      }

      // Translate pixels to index
      pixels = new int[width * height];
      for(p = 0; p < imgpixels.length; p++)
         pixels[p] =
            ((Integer)colors2index.get(new Integer(imgpixels[p]))).intValue();

      buildPixelChars();

      colors2index = null; // done with this
   }

   /*--------------------------------------------------------------------*/
   /**
      Create an XPM Object from a Java Image and a mask image
   */
   public XpmImage(String iname, Component icomp, Image myimg, Image mymask)
   {
      if(mymask == null)
      {
         new XpmImage(iname, icomp, myimg);
         return;
      }

      int[] imgpixels;
      int[] maskpixels;
      PixelGrabber pg;
      int maskw, maskh;
      int p, c;

      name = iname;

      // Get the image pixels
      width  = myimg.getWidth(icomp);
      height = myimg.getHeight(icomp);

      imgpixels = new int[width * height];
      pg = new PixelGrabber(myimg, 0, 0, width, height, imgpixels, 0, width);
      try
      {
         pg.grabPixels();
      } catch (InterruptedException e) {
         System.err.println("interrupted waiting for pixels!");
         return;
      }

      // Get the mask pixels
      maskw = mymask.getWidth(icomp);
      maskh = mymask.getHeight(icomp);

      maskpixels = new int[maskw * maskh];
      pg = new PixelGrabber(mymask, 0, 0, maskw, maskh, maskpixels, 0, maskw);
      try
      {
         pg.grabPixels();
      } catch (InterruptedException e) {
         System.err.println("interrupted waiting for pixels!");
         return;
      }

      // Go!
      if( width == maskw && height == maskh)  // Normal processing
      {
         // Get list of unique colors using hash
         colors2index = new Hashtable(20); // 20 is kinda arbitrary
         for(p = 0; p < imgpixels.length; p++)
            colors2index.put(new Integer(imgpixels[p]), Boolean.TRUE);

         ncolors = colors2index.size() + 1; // Add one for the mask
         colors  = new int[ncolors];

         // Build color map, i.e. hash that associated RGB val with int index
         c = 0;
         for (Enumeration e = colors2index.keys(); e.hasMoreElements(); c++)
         {
            Integer i = (Integer)e.nextElement();
            colors2index.put(i, new Integer(c));
            colors[c] = i.intValue();
         }
         colors[c]  = 0x00CCCCFF; // The mask
         mask_pixel = c;
         hasMask    = true;

         // Translate pixels to index taking mask into account
         pixels = new int[width * height];
         for(p = 0; p < imgpixels.length; p++)
            if(maskpixels[p] == 0xFF000000) // Black means keep pixel
               pixels[p] =
                  ((Integer)colors2index.get(new Integer(imgpixels[p]))).intValue();
            else pixels[p] = mask_pixel; // Set to mask

         buildPixelChars();
      } else {
         // There is an image/mask size mismatch - currently unsupported
         // Plan is to simply mask only the intersection of the two
         System.err.println("Image/mask size mismatch");
      }
      colors2index = null; // done with this
   }

   /*--------------------------------------------------------------------*/
   private void buildPixelChars()
   {
      int cppm, c, j, i2;
      StringBuffer pcsb;

      // Build pixelchar table
      index2pchars = new String[ncolors];

      //----- compute the minimal cpp...
      for (cppm = 1, c = printable.length(); ncolors > c; cppm++)
         c *= printable.length();
      if (cpp < cppm) cpp = cppm;

      //----- ...and build table
      /* Algorithm adapted from that in the 'scan.c' module of the
         xpm-3.4k package.
         Developed by Arnaud Le Hors
         Copyright (C) 1989-95 GROUPE BULL */
      for(i2 = 0; i2 < index2pchars.length; i2++)
      {
         pcsb = new StringBuffer();
         pcsb.append(printable.charAt(c = i2 % printable.length()));
         for(j = 1; j < cpp; j++)
            pcsb.append(printable.charAt(c = ( (i2 - c) /
                                               printable.length()) %
                                         printable.length()));
         index2pchars[i2] = pcsb.toString();
      }
   }


   /*====================================================================*/
   /* ACCESSORS                                                          */
   /*====================================================================*/
   public String getName()   { return name;   }
   public int    getWidth()  { return width;  }
   public int    getHeight() { return height; }

   public void setName(String inname)  { name = inname; }

   /*====================================================================*/
   /* CONVERTERS                                                         */
   /*====================================================================*/
   public Image getImage(Component mycomp)
   {
      /* It would seem like the IndexColorModel would be most appropriate
         for this task, since we basically store the data in index color
         form anyway, including a designated transparent pixel. BUT ICM
         only accepts the color map data in byte[] form - either individual
         channel arrays (red, green, etc.) or a single packed array. We
         store colors in int[] as packed rgba values. So we would have
         to translate the cmap into one of those byte[] forms. This
         doesn't seem a whole lot better than what we do below.

         I suppose one could abstract away that logic into some method,
         (SEE BELOW) but that doesn't mean the work doesn't still have
         to be done... I'm not sure I see much benefit in that. And on
         top of that would have to actually instantiate an ICM Object.
       */

      Image outImg;
      int[] rgbpixels;
      int row, col;
      MemoryImageSource mis;

      rgbpixels = new int[pixels.length];

      for(row = 0; row < height; row++)
         for(col = 0; col < width; col++)
            rgbpixels[row * width + col] = colors[pixels[row * width + col]];

      mis = new MemoryImageSource(width, height, rgbpixels, 0, width);
      outImg = mycomp.createImage(mis);
      return outImg;
   }

   /*--------------------------------------------------------------------*/
   public Image getImage()
   {
      // Toolkit version
      Image outImg;
      int[] rgbpixels;
      int row, col;
      MemoryImageSource mis;

      rgbpixels = new int[pixels.length];

      for(row = 0; row < height; row++)
         for(col = 0; col < width; col++)
            rgbpixels[row * width + col] = colors[pixels[row * width + col]];

      mis = new MemoryImageSource(width, height, rgbpixels, 0, width);
      outImg = Toolkit.getDefaultToolkit().createImage(mis);
      return outImg;
   }

   /*--------------------------------------------------------------------*/
   private byte[] intArrtoPackedByteArr(int[] myints)
   {
      /* Well, here it is... */
      byte[] outbytes = new byte[myints.length * 4]; // Know int = 4 bytes

      // ints are like this
      //    0xAARRGGBB
      // should the byte array have this order?
      // The API docs would seem to indicate otherwise -
      //    { 0xRR, 0xGG, 0xBB, 0xAA }
      for(int i = 0; i < myints.length; i++)
      {
         outbytes[i*4]   = (byte)((myints[i] >> 16) & 0x000000FF); // 0xRR
         outbytes[i*4+1] = (byte)((myints[i] >>  8) & 0x000000FF); // 0xGG
         outbytes[i*4+2] = (byte)((myints[i])       & 0x000000FF); // 0xBB
         outbytes[i*4+3] = (byte)((myints[i] >> 24) & 0x000000FF); // 0xAA
      }

      return outbytes;
   }

   /*--------------------------------------------------------------------*/
   public Image getMask(Component mycomp)
   {
      Image outImg;
      int[] rgbpixels;
      int row, col;
      MemoryImageSource mis;

      rgbpixels = new int[pixels.length];

      if(hasMask)
         for(row = 0; row < height; row++)
            for(col = 0; col < width; col++)
               if(pixels[row * width + col] == mask_pixel)
                  rgbpixels[row * width + col] = 0xFFFFFFFF;
               else
                  rgbpixels[row * width + col] = 0xFF000000;
      else
         for(row = 0; row < height; row++)
            for(col = 0; col < width; col++)
               rgbpixels[row * width + col] = 0xFF000000;

      mis = new MemoryImageSource(width, height, rgbpixels, 0, width);
      outImg = mycomp.createImage(mis);

      return outImg;
   }


   /*====================================================================*/
   /* INPUT                                                              */
   /*====================================================================*/
   public void readXpm(Reader xpmsrc) throws IOException
   {
      BufferedReader br = new BufferedReader(xpmsrc);
      st = new StreamTokenizer(br);

      st.wordChars('_', '_');     // for name
      st.slashStarComments(true); // Ignore comments - for now

      readName();
      readValues();

      pchars2index = new Hashtable(ncolors);
      readColors();
      readPixels();
      pchars2index = null; // No longer needed

      readExtensions();
   }

   /*--------------------------------------------------------------------*/
   private String readValidToken() throws IOException
   {
      boolean done = false;
      int tokVal;

      while(!done)
      {
         tokVal = st.nextToken();
         switch(tokVal)
         {
            case st.TT_EOF:            return null;    // break;
            case st.TT_WORD:           return st.sval; // break;
            default: if(tokVal == '"') return st.sval;    break;
         }
      }

      return null;
   }

   /*--------------------------------------------------------------------*/
   private void readName() throws IOException
   {
      readValidToken();
      readValidToken();
      name = readValidToken();
   }

   /*--------------------------------------------------------------------*/
   private void readValues() throws IOException
   {
      String valueStr = readValidToken();
      parseValues(valueStr);

      index2pchars = new String[ncolors];
      m_colors     = new String[ncolors];
      symbolic     = new String[ncolors];
      g4_colors    = new String[ncolors];
      g_colors     = new String[ncolors];
      colors       = new int[ncolors];
      pixels       = new int[width * height];
   }

   /*--------------------------------------------------------------------*/
   private void readColors() throws IOException
   {
      for(int i = 0; i < ncolors; i++)
      {
         String colorStr = readValidToken();
         parseColor(colorStr, i); // need to know row
      }
   }

   /*--------------------------------------------------------------------*/
   private void readPixels() throws IOException
   {
      for(int i = 0; i < height; i++)
      {
         String pixelStr = readValidToken();
         parsePixels(pixelStr, i); // need to know row
      }
   }

   /*--------------------------------------------------------------------*/
   private void readExtensions() // Just ignore extensions for now...
   {
      String currentLine;

      do
      {
         try { currentLine = readValidToken(); }
         catch(IOException ioe) { currentLine = null; }
      } while(currentLine != null);
   }

   /*====================================================================*/
   /* DATA PARSERS                                                       */
   /*====================================================================*/
   private void parseValues(String data) throws IOException
   {
      StringReader    sr  = new StringReader(data);
      StreamTokenizer vst = new StreamTokenizer(sr);
      int tokVal;
      hasExtensions = false;

      // Standard Values
      //----- Width
      tokVal = vst.nextToken();
      if(tokVal == vst.TT_NUMBER) width = (int)vst.nval;
      else throw new IOException("Width not found");

      //----- Height
      tokVal = vst.nextToken();
      if(tokVal == vst.TT_NUMBER) height = (int)vst.nval;
      else throw new IOException("Height not found");

      //----- ncolors
      tokVal = vst.nextToken();
      if(tokVal == vst.TT_NUMBER) ncolors = (int)vst.nval;
      else throw new IOException("Num Colors not found");

      //----- chars_per_pixel
      tokVal = vst.nextToken();
      if(tokVal == vst.TT_NUMBER) cpp = (int)vst.nval;
      else throw new IOException("Chars per Pixel not found");


      // Optional values
      tokVal = vst.nextToken();
      switch(tokVal)
      {
         // Perhaps should do an error check here to see if the
         // hotspot is actually inside the image
         case st.TT_NUMBER: //----- X hotspot
            x_hotspot = (int)vst.nval;
            tokVal = vst.nextToken();
            if(tokVal == vst.TT_NUMBER) y_hotspot = (int)vst.nval;
            else throw new IOException("Y Hotspot not found");
            break;
         case st.TT_WORD:   //----- Extension
            if(vst.sval.compareTo("XPMEXT") == 0) hasExtensions = true;
            // Even if another String is encountered - just ignore it
            return;
         case st.TT_EOF:
            return;
      }

      tokVal = vst.nextToken();
      if(tokVal == st.TT_WORD) //----- Extension
         if(vst.sval.compareTo("XPMEXT") == 0) hasExtensions = true;

      // Even if other stuff  encountered - just ignore it
      return;
   }


   /*--------------------------------------------------------------------*/
   private void parseColor(String data, int row) throws IOException
   {
      boolean found = false, cfound = false;
      String pc, colordata;
      String key, colorid;

      int    c_val  = 0;
      String m_val  = null;
      String g4_val = null;
      String g_val  = null;
      String s_val  = null;

      StringReader sr;
      StreamTokenizer cst;

      /* PROBLEM:
            Oooo... this is a little tricky. StreamTokenizer will split
            the data off of whitespace which is usually what we want
            EXCEPT for the fact that XPMs can use the ' ' (Space) char as
            one of the pixel chars so StreamTokenizer could mess it up.
            After getting the chars we DO want to split off of whitespace.
         SOLUTION:
            Manually grab the first 'cpp' chars as a substring and
            StreamTokenizer. This makes the ASSUMPTION that there are
            no extraneous characters at the beginning of the string, i.e.
            the key chars are at the very beginning of the string. */

      pc = data.substring(0,cpp);
      colordata  = data.substring(cpp);

      pchars2index.put(pc, new Integer(row));
      index2pchars[row] = pc;

      sr = new StringReader(colordata);
      cst = new StreamTokenizer(sr);
      cst.wordChars('_', '_'); // for symbolics
      cst.wordChars('#', '#'); // for RGB vals

      // Tokenize and parse the color string
      // WARNING: This will not handle multiword color names properly
      int tokVal = cst.TT_WORD;
      while(tokVal != cst.TT_EOF)
      {
         tokVal = cst.nextToken();
         if(tokVal != cst.TT_EOF)
         {
            if(tokVal == cst.TT_WORD) key = cst.sval;
            else throw new IOException("Color key not found");

            tokVal = cst.nextToken();
            if(tokVal == cst.TT_WORD) colorid = cst.sval;
            else throw new IOException("Color data not found");

            if(key.compareTo("m")  == 0) { m_val  = colorid; found = true; }
            if(key.compareTo("g")  == 0) { g_val  = colorid; found = true; }
            if(key.compareTo("g4") == 0) { g4_val = colorid; found = true; }
            if(key.compareTo("s")  == 0)   s_val  = colorid;
            if(key.compareTo("c")  == 0)
            {
               c_val = decodeColor(colorid);
               if((c_val & 0xFF000000) == 0) { hasMask = true; mask_pixel = row; }
               cfound = found = true;
            }
         }
      }

      if(!found) throw new IOException("No color data found for [" +pc+ "]");
      if(!cfound) // No color ('c') found; grab one of the others
      {
         // If 'found' is true, one of these must be available
         if(g4_val != null)     c_val = decodeColor(g4_val);
         else if(g_val != null) c_val = decodeColor(g_val);
         else if(m_val != null) c_val = decodeColor(m_val);
      }

      colors[row] = c_val;
   }


   /*--------------------------------------------------------------------*/
   private int decodeColor(String colorStr) throws IOException
   {
      int outval = 0;

      // 0xAARRGGBB
      // alpha components of 255 (fully opaque).
      if(colorStr.compareTo("None") == 0) outval = 0x00cccccc;
      else if(colorStr.startsWith("#")) // This is an RGB value
      {
         switch(colorStr.length())
         {
            case 7:
               // Can't parse Strings >= 0x80000000 with Integer
               outval = (int)Long.parseLong("ff" + colorStr.substring(1,7), 16);
               break;
            case 13:
               // A 12 digit color str - shorten it
               String shortColor = "ff" + colorStr.substring(1,3) +
                  colorStr.substring(5,7) + colorStr.substring(9,11);
               outval = (int)Long.parseLong(shortColor, 16);
               break;
            default:
               throw new IOException("Color strings of length " +
                                     colorStr.length() +
                                     "are not supported");
         }
      }

      // Otherwise this must be a named color
      else outval = RGBColorNames.colorName2RGB(colorStr);

      return outval;
   }


   /*--------------------------------------------------------------------*/
   private void parsePixels(String data, int row) throws IOException
   {
      for(int i = 0; i < width; i++) // pixelchars extracted from this line
      {
         String pc = data.substring(i * cpp, (i+1) * cpp);
         pixels[row * width + i] = ((Integer)pchars2index.get(pc)).intValue();
      }
   }


   /*====================================================================*/
   /* OUTPUT                                                             */
   /*====================================================================*/
   public void write(Writer datadest) throws IOException
   {
      BufferedWriter bw = new BufferedWriter(datadest);

      writeName(bw);
      writeValues(bw);
      writeColors(bw);
      writePixels(bw);
      writeExtensions(bw);
      bw.write("}\n");

      bw.flush();
   }

   /*--------------------------------------------------------------------*/
   private void writeName(Writer nw) throws IOException
   {
      nw.write("/* XPM */\n");
      nw.write("static char * " + name  + "[]={\n");
   }

   /*--------------------------------------------------------------------*/
   private void writeValues(Writer vw) throws IOException
   {
      vw.write("\"" + width   + " " + height +
               " "  + ncolors + " " + cpp);
      if(x_hotspot >= 0 && y_hotspot >= 0)
         vw.write(" " + x_hotspot + " " + y_hotspot);
      if(hasExtensions) vw.write("  XPMEXT");
      vw.write("\",\n");
   }

   /*--------------------------------------------------------------------*/
   private void writeColors(Writer cw) throws IOException
   {
      cw.write("/* colors */\n");
      for(int i = 0; i < ncolors; i++)
      {
         cw.write("\"" + index2pchars[i] + "  c  ");
         if(hasMask && i == mask_pixel)
            cw.write("None");
         else
            cw.write("#" + Integer.toHexString(colors[i]).substring(2,8));
         cw.write("\",\n");
      }
   }

   /*--------------------------------------------------------------------*/
   private void writePixels(Writer pw) throws IOException
   {
      pw.write(pixels_cmt +  "\n");
      for(int row = 0; row < height; row++)
      {
         pw.write("\"");
         for(int col = 0; col < width; col++)
            pw.write(index2pchars[pixels[row * width + col]]);
         pw.write("\"");
         if(row < height - 1) pw.write(",\n");
      }
   }

   /*--------------------------------------------------------------------*/
   private void writeExtensions(Writer ew) throws IOException
   {
      if(hasExtensions)
      {
         //ew.write(",\n");
         ew.write("\n");
         ew.write("/* Extensions */\n");
         ew.write("/* XPM Extensions are currently unsupported */\n");
      }
   }
}

/* All an XPM file really is is an array of strings (surrounded) perhaps interspersed with comments. For the time being we will dispense with the comments (worry about them later) and concentrate on the strings. The strings are grouped in a few key sections:
- Values: Width, Height, #Colors, bytes/pixel
- Colors: Code TableId ColorName OR RGB
- Pixels: lines of chars defined in color table
- Extensions: ?
*/
