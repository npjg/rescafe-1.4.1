/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/icnsResourceHandler.java,v 1.4 2000/12/11 02:48:28 gbsmith Exp $ */

import com.sun.media.jai.codec.*; // Java Advanced Imaging - tools for image I/O

import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;

import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 2000 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: icnsResourceHandler.java,v $
 * Revision 1.4  2000/12/11 02:48:28  gbsmith
 * Used Composite classes to apply 8-bit masks to images making
 * an alpha channel. Then used Java Advanced Imaging calls to save
 * the images in PNG format (which supports full alpha channel).
 *
 * Revision 1.3  2000/12/01 07:01:18  gbsmith
 * Finally! Used information from Peter Stuer, builder of IconShop, to
 * add methods for 32-bit subtypes!
 *
 * Revision 1.2  2000/11/27 19:51:37  gbsmith
 * Added a bunch of additional subtypes along with processing code
 * for those subtypes. Still no 32-bit icons or saving though.
 *
 * Revision 1.1  2000/05/24 06:09:41  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
public class icnsResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   private static final String[] basicColumnNames = { "ResID", "Name", "Size" };

   private static final String[] subtypes =
   { "ICN#", "Imask", "icl4", "icl8", "il32", "l8mk",
     "ics#", "imask", "ics4", "ics8", "is32", "s8mk",
     "ich#", "hmask", "ich4", "ich8", "ih32", "h8mk",
     "icm#", "mmask", "icm4", "icm8", "im32" //, "m8mk"
                                   // "it32", "t8mk"
   };

   TableCellRenderer renderer = new IconRenderer();

   String types[];
   String mytypes[] = { "icns" };

   Resource  myResArray[];
   String    icon_names[];
   String    columnNames[];
   Hashtable theImgs;

   String myname    = "icnsResourceHandler";
   String myversion = "v1.0";

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: icnsResourceHandler.java,v 1.4 2000/12/11 02:48:28 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes() { return mytypes;  }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      Resource currentRes;

      myResArray = resData.getResArray();
      theImgs    = new Hashtable();

      icon_names = new String[myResArray.length];
      types      = new String[myResArray.length];

      for( int i = 0; i < myResArray.length; i++)
      {
         icon_names[i] = null;
         currentRes    = myResArray[i];
         read(i, currentRes.getData());
         icon_names[i] = currentRes.getName();

         // Get resource name from related types
         for(int t = 0; t < subtypes.length; t++)
            if( resMod.contains(subtypes[t], myResArray[i].getID()) &&
                icon_names[i] == null)
            {
               currentRes =
                  resMod.getResource(subtypes[t], myResArray[i].getID());
               if(currentRes != null) icon_names[i] = currentRes.getName();
            }
      }
   }

   /*--------------------------------------------------------------------*/
   private void read(int index, byte rawData[])
   {
      DataInputStream dis =
         new DataInputStream(new ByteArrayInputStream(rawData));
      byte subname[] = new byte[4];
      int  fullsize  = 0, subsize = 0;
      byte subData[];

      StringBuffer tmpnames = new StringBuffer();

      try
      {
         dis.readFully(subname); // skip main type
         fullsize = dis.readInt();
         fullsize -= 8;

         // Loop through subtypes
         while(fullsize > 0)
         {
            dis.readFully(subname);
            subsize = dis.readInt();
            fullsize -= subsize;

            tmpnames.append(new String(subname) + " (" + (subsize-8) + "), ");

            subData = new byte[subsize - 8];
            dis.readFully(subData);
            process(new String(subname), index, subData);
         }

      } catch (IOException ioe) { }

      types[index] = new String(tmpnames);
   }

   /*--------------------------------------------------------------------*/
   private void process(String type, int i, byte rawData[])
   {
      Image curr[];


      if(!theImgs.containsKey(type))
      {
         theImgs.put(type, new Image[myResArray.length]);
         System.out.println("*** NOTE: saw subtype '" + type + "'");
      }

      // These have masks so it is a special case
      if(type.compareTo("ics#") == 0 && !theImgs.containsKey("imask"))
         theImgs.put("imask", new Image[myResArray.length]);
      if(type.compareTo("ICN#") == 0 && !theImgs.containsKey("Imask"))
         theImgs.put("Imask", new Image[myResArray.length]);
      if(type.compareTo("ich#") == 0 && !theImgs.containsKey("hmask"))
         theImgs.put("hmask", new Image[myResArray.length]);

      // Dispatch processing to proper method
      curr = (Image[])theImgs.get(type);
      if     (type.compareTo("ics4") == 0) curr[i] = process_ics4(rawData);
      else if(type.compareTo("ics8") == 0) curr[i] = process_ics8(rawData);
      else if(type.compareTo("is32") == 0) curr[i] = process_is32(rawData);
      else if(type.compareTo("s8mk") == 0) curr[i] = process_s8mk(rawData);

      else if(type.compareTo("icl4") == 0) curr[i] = process_icl4(rawData);
      else if(type.compareTo("icl8") == 0) curr[i] = process_icl8(rawData);
      else if(type.compareTo("il32") == 0) curr[i] = process_il32(rawData);
      else if(type.compareTo("l8mk") == 0) curr[i] = process_l8mk(rawData);

      else if(type.compareTo("ich4") == 0) curr[i] = process_ich4(rawData);
      else if(type.compareTo("ich8") == 0) curr[i] = process_ich8(rawData);
      else if(type.compareTo("ih32") == 0) curr[i] = process_ih32(rawData);
      else if(type.compareTo("h8mk") == 0) curr[i] = process_h8mk(rawData);

      // 128 x 128 icons ?!
      //else if(type.compareTo("it32") == 0) curr[i] = process_it32(rawData);
      //else if(type.compareTo("t8mk") == 0) curr[i] = process_t8mk(rawData);

      // Masks are a special case
      else if(type.compareTo("ics#") == 0)
      {
         curr[i] = process_ics(rawData);
         curr    = (Image[])theImgs.get("imask");
         curr[i] = process_ics_mask(rawData);
      } else if(type.compareTo("ICN#") == 0) {
         curr[i] = process_ICN(rawData);
         curr    = (Image[])theImgs.get("Imask");
         curr[i] = process_ICN_mask(rawData);
      } else if(type.compareTo("ich#") == 0) {
         curr[i] = process_ich(rawData);
         curr    = (Image[])theImgs.get("hmask");
         curr[i] = process_ich_mk(rawData);
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int cn, st;
      TableColumn tc;
      DefaultTableModel tmpModel;
      Image  currentImg;
      Vector seenSubtypes;

      Resource myResArray[] = resData.getResArray();

      // Build new array of column names
      columnNames    = new String[3 + theImgs.size()];
      columnNames[0] = basicColumnNames[0];
      columnNames[1] = basicColumnNames[1];
      columnNames[2] = basicColumnNames[2];

      seenSubtypes = new Vector(theImgs.keySet());

      // Make column for the known subtypes in the array
      cn=3;
      for(st=0; st < subtypes.length; st++)
         if(seenSubtypes.contains(subtypes[st]))
         {
            columnNames[cn++] = subtypes[st];
            seenSubtypes.removeElement(subtypes[st]);
         }

      // Add any unknown subtypes encountered
      for(st=0; st < seenSubtypes.size(); st++)
         columnNames[cn++] = (String)seenSubtypes.elementAt(st);

      tmpModel = new DefaultTableModel( columnNames, myResArray.length );

      for( int i = 0; i < myResArray.length; i++)
      {
         // "ResID", "Name", "Size" -------------------------------------
         tmpModel.setValueAt( new Short(myResArray[i].getID()),  i,  0 );
         tmpModel.setValueAt( icon_names[i],                     i,  1 );
         tmpModel.setValueAt( new Integer(myResArray[i].size()), i,  2 );
         //--------------------------------------------------------------

         // Icons -------------------------------------------------------
         for(cn=3; cn < columnNames.length; cn++)
            tmpModel.setValueAt(fetchIcon(i, cn), i, cn);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(36); //36

      // IMPORTANT! Add decorator first THEN set Icon Renderers afterwards.
      //            This prevents the renders from being trampled by the
      //            Decorator model.
      //
      addDecorator();
      for(cn = 3; cn < columnNames.length; cn++)
         if(columnNames[cn] != null)
         {
            tc = resTable.getColumn(columnNames[cn]);
            tc.setCellRenderer(renderer);
         }

      optimizeColumnWidth();

      JScrollPane rtsp = new JScrollPane(resTable);
      setLayout( new BorderLayout() );
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   private Object fetchIcon(int row, int col)
   {
      String colname = columnNames[col];
      String notFound = "n/a";
      Image[] curr;

      if(colname   == null)             return notFound;
      if(!theImgs.containsKey(colname)) return notFound;

      curr = (Image[])theImgs.get(colname);
      if(curr      == null)             return notFound;
      if(curr[row] == null)             return notFound;

      return curr[row];
   }

   /*--------------------------------------------------------------------*/
   protected Image process_32bit( byte rawData[], int dim )
   {
      // i[x]32 is exactly the same as icl8 except that each pixel is a 32-bit
      // word of the format ARGB.
      // - BUT -
      // Indeed, if the size is less than 4096 the bitmap is compressed using
      // RLE. The trick is that the compression is per color channel.
      //
      // - Peter Stuer <Peter.Stuer@pandora.be>

      BufferedImage bi; // BufferedImage is a Java2D subclass of Image
      int iconData[] = new int[dim * dim];

      if(rawData.length == dim * dim * 4) // Full size - 4 bytes/pixel
         // transform to intermediate ints - is this necessary?
         //    Isn't there an "automatic" way to do this?
         //    Can't we create an ARGB image straight from the bytes?
         for(int i = 0; i < dim * dim; i++)
         {
            //             AARRGGBB
            iconData[i] = 0xFF000000;
            // iconData[i] |= (int)(rawData[i*4]   << 24) & 0xFF000000; // Alpha
            iconData[i] |= (int)(rawData[i*4+1] << 16) & 0x00FF0000; // Red
            iconData[i] |= (int)(rawData[i*4+2] <<  8) & 0x0000FF00; // Green
            iconData[i] |= (int)(rawData[i*4+3])       & 0x000000FF; // Blue
         }
      else RLE32_decode(rawData, iconData); // The 'ix32' is RLE compressed

      bi = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
      bi.setRGB(0, 0, dim, dim, iconData, 0, dim);
      return bi;
   }

   /*--------------------------------------------------------------------*/
   private void RLE32_decode(byte rawData[], int outData[])
   {
      // Adapted from a C algorithm provided by
      // Peter Stuer <Peter.Stuer@pandora.be>

      // PRE: Assume outData is allocated and of sufficient size
      int myshift, mymask, r, y, i, len, val;

      // Alpha
      myshift = 24;
      mymask  = 0xFF000000;
      r = 0;

      // Drop in a fully opaque alpha channel - apply mask later
      for(i = 0; i < outData.length; i++)  outData[i] |= 0xFF000000;

      // Red, Green, Blue
      while(myshift > 0) // 24, 16, 8, 0
      {
         myshift -= 8;   // Next byte...
         mymask  >>>= 8; // Right shift in zeroes
         y = 0;
         while(y < outData.length)
            if( (rawData[r] & 0x80) == 0)
            {
               // top bit is clear - run of various vals to follow
               len = (int)(0xFF & rawData[r++]) + 1;   // 1 <= len <= 128
               for(i = 0; i < len; i++)
                  outData[y++] |= (int)(rawData[r++] << myshift) & mymask;
            } else {
               // top bit is set - run of one val to follow
               len = (int)(0xFF & rawData[r++]) - 125; // 3 <= len <= 130
               val = (int)(rawData[r++] <<  myshift) & mymask;
               for(i = 0; i < len; i++) outData[y++] |= val;
            }
      }
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ich8( byte rawData[] )
   {
      MemoryImageSource mis =
      new MemoryImageSource(48, 48, icm256, rawData, 0, 48);
      return createImage(mis);
   }

   /*--------------------------------------------------------------------*/
   protected Image process_8bit_mk( byte rawData[], int dim )
   {
      BufferedImage bi;
      int maskData[] = new int[dim * dim]; // These masks are all square

      // Shift bytes into Alpha pos leaving RGB at 0 (Black)
      for(int i = 0; i < dim * dim; i++) maskData[i] = rawData[i] << 24;
      bi = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
      bi.setRGB(0, 0, dim, dim, maskData, 0, dim);
      return bi;
   }

   /*--------------------------------------------------------------------*/
   protected Image process_ich(byte rd[] ) { return process_1bit(rd, 48, false); }
   protected Image process_ich_mk(byte rd[]) { return process_1bit(rd, 48, true); }
   protected Image process_ich4(byte rd[]) { return process_4bit(rd, 48); }
   protected Image process_s8mk(byte rd[]) { return process_8bit_mk(rd, 16); }
   protected Image process_l8mk(byte rd[]) { return process_8bit_mk(rd, 32); }
   protected Image process_h8mk(byte rd[]) { return process_8bit_mk(rd, 48); }
   protected Image process_t8mk(byte rd[]) { return process_8bit_mk(rd, 128); }
   protected Image process_is32(byte rd[]) { return process_32bit(rd, 16); }
   protected Image process_il32(byte rd[]) { return process_32bit(rd, 32); }
   protected Image process_ih32(byte rd[]) { return process_32bit(rd, 48); }
   protected Image process_it32(byte rd[]) { return process_32bit(rd, 128); }

   /*--------------------------------------------------------------------*/
   public void save( File savedir )
   {
      StringBuffer tmpfilename;
      String filename, imgname, saveType, subType;
      Enumeration subtypeKeys;
      File outfile, subtypedir;
      FileWriter fw;
      FileOutputStream fos;
      XpmImage xpmout;
      Image imgToSave[], maskToSave[];
      BufferedImage interbi, outbi;
      Graphics2D g2;

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      Resource myResArray[] = resData.getResArray();
      saveType = resData.getID();
      System.out.println("Saving resources of type \'" + saveType + "\'");

      // The type 'icns' is a bit of a special case since it is a type
      // composed of other subtypes. Along these lines we will create
      // subdirs in the given saveDir corresponding to the various subtypes.
      // The datafiles for each subtype will then be saved inside.

      // Iterate over the hash 'theImgs' and attempt to create subdirs
      // then save all of that type.
      subtypeKeys = theImgs.keys();
      while(subtypeKeys.hasMoreElements())
      {
         subType = (String)subtypeKeys.nextElement();

         // Don't save masks - will apply them
         if(subType.endsWith("mask")) continue;
         if(subType.endsWith("8mk"))  continue;

         System.err.println("\tsubtype = " + subType);

         maskToSave = null;
         imgToSave = (Image[])theImgs.get(subType);
         if(imgToSave == null)
         {
            System.err.println("WARNING: No " + subType + "icons to save");
            continue;
         }

         // Match current type with appropriate mask
         if(subType.compareTo("ics#") == 0 ||
            subType.compareTo("ics4") == 0 ||
            subType.compareTo("ics8") == 0 )
            maskToSave = (Image[])theImgs.get("imask");
         if(subType.compareTo("ICN#") == 0 ||
            subType.compareTo("icl4") == 0 ||
            subType.compareTo("icl8") == 0 )
            maskToSave = (Image[])theImgs.get("Imask");
         if(subType.compareTo("ich#") == 0 ||
            subType.compareTo("ich4") == 0 ||
            subType.compareTo("ich8") == 0 )
            maskToSave = (Image[])theImgs.get("hmask");
         if(subType.compareTo("icm#") == 0 ||
            subType.compareTo("icm4") == 0 ||
            subType.compareTo("icm8") == 0 )
            maskToSave = (Image[])theImgs.get("mmask");
         if(subType.compareTo("is32") == 0)
            maskToSave = (Image[])theImgs.get("s8mk");
         if(subType.compareTo("il32") == 0)
            maskToSave = (Image[])theImgs.get("l8mk");
         if(subType.compareTo("ih32") == 0)
            maskToSave = (Image[])theImgs.get("h8mk");
         if(subType.compareTo("im32") == 0)
            maskToSave = (Image[])theImgs.get("m8mk");

         // Make a dir for the subtype
         subtypedir = new File(savedir, subType);
         if(subtypedir.exists() && !subtypedir.isDirectory())
            subtypedir.delete();
         if(!subtypedir.exists()) subtypedir.mkdir();

         for(int i=0; i < myResArray.length; i++)
         {
            tmpfilename = new StringBuffer("" + myResArray[i].getID() );

            // Try to find resource name
            if(myResArray[i].getName() != null)
            {
               imgname = myResArray[i].getName();
               tmpfilename.append("_" + myResArray[i].getName());
            } else if(icon_names[i] != null) {
               imgname = icon_names[i];
               tmpfilename.append("_" + icon_names[i]);
            } else imgname = "icon" + myResArray[i].getID();

            if(subType.endsWith("32")) tmpfilename.append(".png");
            else                       tmpfilename.append(".xpm");

            filename = tmpfilename.toString().
               replace(' ', '_').
               replace(File.separatorChar, '+');
            imgname = imgname.replace(' ', '_');

            if(imgToSave[i] == null)
            {
               System.err.println("WARNING: Image " + subType + ":" +
                                  imgname + " seems to be null!");
               continue;
            }

            // Actually save the image
            try
            {
               outfile = new File(subtypedir, filename);
               fos = new FileOutputStream(outfile);

               if(subType.endsWith("32")) // 32-bit image, save as PNG
               {
                  interbi = (BufferedImage)imgToSave[i];
                  if(maskToSave == null || maskToSave[i] == null)
                     outbi = interbi; // just dump the images
                  else  // Apply mask if one exists
                  {
                     outbi = new BufferedImage(interbi.getWidth(),
                                               interbi.getHeight(),
                                               BufferedImage.TYPE_INT_ARGB);
                     g2 = outbi.createGraphics();
                     g2.drawImage(interbi, null, 0, 0);

                     // Here is where the mask gets applied
                     interbi = (BufferedImage)maskToSave[i];
                     g2.setComposite(AlphaComposite.DstIn);
                     g2.drawImage(interbi, null, 0, 0);
                  }

                  // NOTE: According to the literature that I have read
                  // (mainly from Sun) this JAI/PNG stuff is in a state of
                  // flux. But it is the only thing I found that works
                  // correctly for PNG.

                  // Create the ParameterBlock.
                  PNGEncodeParam param =
                     PNGEncodeParam.getDefaultEncodeParam(outbi);

                  //Create the PNG image encoder.
                  ImageEncoder enc = ImageCodec.createImageEncoder("PNG", fos,
                                                                   param);
                  enc.encode(outbi); // Save...
               } else { // Should we even bother keeping the XPM stuff?
                  fw = new FileWriter(outfile);
                  xpmout = (maskToSave[i] == null)?
                     new XpmImage(imgname, this, imgToSave[i]):
                     new XpmImage(imgname, this, imgToSave[i], maskToSave[i]);
                  xpmout.write(fw);
               }
            } catch (Exception whatever) {
               System.err.println("ERROR: While saving, got exception " + whatever );
            }
         }
      }
   }

   /*--------------------------------------------------------------------*/
   public String[] about( )
   {
      String[] pluginfo =
      { myname,
        myversion,
        "by G. Brannon Smith",
        " ",
        "This plugin attempts to handle the newer icns 32-bit icon."
      };
      return pluginfo;
   }
}
