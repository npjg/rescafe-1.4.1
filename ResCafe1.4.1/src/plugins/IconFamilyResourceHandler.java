/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/IconFamilyResourceHandler.java,v 1.11 2000/05/24 07:57:27 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;

import java.io.File;
import java.io.FileWriter;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: IconFamilyResourceHandler.java,v $
 * Revision 1.11  2000/05/24 07:57:27  gbsmith
 * Fixed the calls to XpmImage class methods to prevent empty XPMs from
 * being written from null Images.
 *
 * Revision 1.10  2000/05/24 06:49:08  gbsmith
 * Updated version number to 1.2
 *
 * Revision 1.9  2000/05/24 06:17:59  gbsmith
 * Moved column width optimizer code and column sorting code up in
 * the hierarchy so siblings could take advantage of it.
 * Started using custom, homebrew XpmImage class instead of Jimi
 * for saving Icons because it properly and easily handles masks.
 * Also had to fiddle with the the icon naming code to handle
 * errant file separator chars that were getting mixed in.
 *
 * Revision 1.8  1999/12/19 07:43:42  gbsmith
 * Added sorting Decorator functionality for sorting on text columns
 * with mouse clicks.
 *
 * Revision 1.7  1999/12/19 05:51:57  gbsmith
 * Moved specific icon type processor methods into superclass
 * (for use in sister classes)
 *
 * Revision 1.6  1999/12/19 04:54:20  gbsmith
 * Fixed Mask image saving problem. Added code to automatically adjust columns
 * to optimum width.
 *
 * Revision 1.5  1999/10/19 06:00:46  gbsmith
 * Overrode 'about' method. Added copyright notice.
 *
 * Revision 1.4  1999/10/18 03:00:29  gbsmith
 * Added code for icon name collecting. If a resource of given type has
 * no name, matching resources of other types are checked for a name.
 *
 * Revision 1.3  1999/10/17 23:13:38  gbsmith
 * Now gets color models from palette methods.
 *
 * Revision 1.2  1999/10/17 20:32:04  gbsmith
 * Made needed color models once to share throughout instance. Added "n/a"
 * text for unavailable Icon images. Made class imports more explicit.
 *
 * Revision 1.1  1999/10/16 02:20:06  gbsmith
 * Initial revision
 *
 */


/*=======================================================================*/
public class IconFamilyResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   //JList resList;
   //SortDecorator decorator;

   protected static final String[] columnNames =
   { "ResID", "Name", "Size",
     "ICN#", "Imask", "icl4", "icl8", "ics#", "imask", "ics4", "ics8" };

   TableCellRenderer renderer = new IconRenderer();

   //    B&W     Masks        4-bit    8-bit
   Image ICNs[], ICN_masks[], icl4s[], icl8s[];
   Image icss[], ics_masks[], ics4s[], ics8s[];

   String icon_names[];

   String mytypes[] = { "ICN#", "icl4", "icl8", "ics#", "ics4", "ics8" };

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: IconFamilyResourceHandler.java,v 1.11 2000/05/24 07:57:27 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return mytypes;
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      /*
        This will be a multi-type handler that will access all other types
        it handles when anyone is called. It will match and display the
        corresponding resources together.

        Must figure out which type we were called with,
        Process it THEN
        Fetch and Process match from OTHER types.
      */

      Resource myResArray[] = resData.getResArray();
      Resource currentRes;

      // Allocate image arrays
      ICNs      = new Image[myResArray.length];
      ICN_masks = new Image[myResArray.length];
      icl4s     = new Image[myResArray.length];
      icl8s     = new Image[myResArray.length];

      icss      = new Image[myResArray.length];
      ics_masks = new Image[myResArray.length];
      ics4s     = new Image[myResArray.length];
      ics8s     = new Image[myResArray.length];

      icon_names = new String[myResArray.length];

      for( int i = 0; i < myResArray.length; i++)
      {
         icon_names[i] = null;
         currentRes    = myResArray[i];
         process(resData.getID(), i, currentRes.getData());
         icon_names[i] = currentRes.getName();

         for(int t = 0; t < mytypes.length; t++)
         {
            if(mytypes[t].compareTo(resData.getID()) == 0) continue;
            if(resMod.contains(mytypes[t], myResArray[i].getID()))
            {
               currentRes =
                  resMod.getResource(mytypes[t], myResArray[i].getID());
               process(mytypes[t], i, currentRes.getData());
               if(icon_names[i] == null) icon_names[i] = currentRes.getName();
            }
         }
      }
   }

   /*--------------------------------------------------------------------*/
   private void process(String type, int index, byte rawData[])
   {
      if(type.compareTo("ICN#") == 0)
      {
         ICNs[index]      = process_ICN( rawData );
         ICN_masks[index] = process_ICN_mask( rawData );
      }

      if(type.compareTo("icl4") == 0) icl4s[index] = process_icl4( rawData );
      if(type.compareTo("icl8") == 0) icl8s[index] = process_icl8( rawData );

      if(type.compareTo("ics#") == 0)
      {
         icss[index]      = process_ics( rawData );
         ics_masks[index] = process_ics_mask( rawData );
      }

      if(type.compareTo("ics4") == 0) ics4s[index] = process_ics4( rawData );
      if(type.compareTo("ics8") == 0) ics8s[index] = process_ics8( rawData );
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      TableColumn tc;
      DefaultTableModel tmpModel;
      Image currentImg;

      Resource myResArray[] = resData.getResArray();
      tmpModel = new DefaultTableModel( columnNames, myResArray.length );

      for( int i = 0; i < myResArray.length; i++)
      {
         // "ResID", "Name", "Size" -------------------------------------
         tmpModel.setValueAt( new Short(myResArray[i].getID()),  i,  0 );
         tmpModel.setValueAt( icon_names[i],                     i,  1 );
         tmpModel.setValueAt( new Integer(myResArray[i].size()), i,  2 );
         //--------------------------------------------------------------

         // "ICN#", "Imask", "icl4", "icl8" -----------------------------
         currentImg = ICNs[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  3 );
         else                   tmpModel.setValueAt("n/a",       i,  3 );

         currentImg = ICN_masks[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  4 );
         else                   tmpModel.setValueAt("n/a",       i,  4 );

         currentImg = icl4s[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  5 );
         else                   tmpModel.setValueAt("n/a",       i,  5 );

         currentImg = icl8s[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  6 );
         else                   tmpModel.setValueAt("n/a",       i,  6 );
         //--------------------------------------------------------------

         // "ics#", "imask", "ics4", "ics8" -----------------------------
         currentImg = icss[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  7 );
         else                   tmpModel.setValueAt("n/a",       i,  7 );

         currentImg = ics_masks[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  8 );
         else                   tmpModel.setValueAt("n/a",       i,  8 );

         currentImg = ics4s[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i,  9 );
         else                   tmpModel.setValueAt("n/a",       i,  9 );

         currentImg = ics8s[i];
         if(currentImg != null) tmpModel.setValueAt(currentImg,  i, 10 );
         else                   tmpModel.setValueAt("n/a",       i, 10 );
         //--------------------------------------------------------------
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(36);

      // IMPORTANT! Add decorator first THEN set Icon Renderers afterwards.
      //            This prevents the renders from being trampled by the
      //            Decorator model.
      //
      addDecorator();

      // Set renderered so Icons will actually be drawn in the table
      for(int cn = 3; cn < columnNames.length; cn++)
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
   public String[] about( )
   {
      String[] pluginfo =
      { "IconFamilyResourceHandler",
        "v1.2",
        "by G. Brannon Smith",
        " ",
        "This plugin handles types that are part of the standard",
        "Macintosh Icon family, registering itself for all such",
        "types. Other types in the family are also grabbed and",
        "presented regardless of which type it was called with."
      };

      return pluginfo;
   }

   /*--------------------------------------------------------------------*/
   public void save( File savedir )
   {
      StringBuffer tmpfilename;
      String filename, imgname, saveType;

      File outfile;
      FileWriter fw;

      XpmImage xpmout;

      Image imgToSave[],  maskToSave[];

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      Resource myResArray[] = resData.getResArray();
      saveType = resData.getID();

      if( saveType.compareTo("ICN#") == 0)
      {
         imgToSave  = ICNs;
         maskToSave = ICN_masks;
      } else if( saveType.compareTo("icl4") == 0 ) {
         imgToSave  = icl4s;
         maskToSave = ICN_masks;
      } else if( saveType.compareTo("icl8") == 0 ) {
         imgToSave  = icl8s;
         maskToSave = ICN_masks;
      } else if( saveType.compareTo("ics#") == 0 ) {
         imgToSave  = icss;
         maskToSave = ics_masks;
      } else if( saveType.compareTo("ics4") == 0 ) {
         imgToSave  = ics4s;
         maskToSave = ics_masks;
      } else if( saveType.compareTo("ics8") == 0 ) {
         imgToSave  = ics8s;
         maskToSave = ics_masks;
      } else return;

      System.out.println("Saving resources of type \'" + saveType + "\'");
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
         } else
            imgname = "icon" + myResArray[i].getID();
         tmpfilename.append(".xpm");

         filename = tmpfilename.toString().
                replace(' ', '_').
                replace(File.separatorChar, '+');
         imgname = imgname.replace(' ', '_');

         try
         {
            outfile = new File(savedir, filename);
            fw = new FileWriter(outfile);
            if(imgToSave[i] == null)
               System.err.println("Image " + imgname + " seems to be null!");
            else
            {
               if(maskToSave[i] == null)
                  xpmout = new XpmImage(imgname, this, imgToSave[i]);
               else
                  xpmout = new XpmImage(imgname, this, imgToSave[i], maskToSave[i]);
               xpmout.write(fw);
            }
         } catch (Exception whatever) {
            System.err.println("ERROR: While saving, got exception " + whatever );
         }
      }
   }
}
