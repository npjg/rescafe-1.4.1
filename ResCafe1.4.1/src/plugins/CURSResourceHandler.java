/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/CURSResourceHandler.java,v 1.4 2000/05/25 07:57:59 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

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
 * $Log: CURSResourceHandler.java,v $
 * Revision 1.4  2000/05/25 07:57:59  gbsmith
 * Switched from Jimi to XpmImage which means proper handling
 * of masks.
 *
 * Revision 1.3  1999/10/21 21:07:41  gbsmith
 * Added copyright notice. Made class imports more explicit.
 * Changed color model call.
 *
 * Revision 1.2  1999/10/04 21:50:30  gbsmith
 * Adapted to new initialization technique. Implemented save method.
 *
 * Revision 1.1  1999/09/30 05:19:59  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
public class CURSResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   private static final String[] columnNames = {
      "ResID", "Name", "Size", "Icon", "Mask", "x", "y" };
   IndexColorModel icm;
   Image icons[];
   Image masks[];
   int x[];
   int y[];

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: CURSResourceHandler.java,v 1.4 2000/05/25 07:57:59 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes() { return new String[]{"CURS"};   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      int i, j, b;
      MemoryImageSource mis;
      byte rawData[];
      byte iconData[];
      byte maskData[];

      Resource myResArray[] = resData.getResArray();

      icm = MacStandard16Palette.getColorModel();

      icons = new Image[myResArray.length];
      masks = new Image[myResArray.length];
      x = new int[myResArray.length];
      y = new int[myResArray.length];

      for( i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();

         // Grab icon data
         iconData = new byte[256];
         for ( j = 0; j < 32; j++)
            for ( b = 0; b < 8; b++)
               iconData[j*8+b] = (byte)((rawData[j] &
                                        (0x80 >>> b)) > 0? 0x0F: 0x00);

         mis = new MemoryImageSource(16, 16, icm, iconData, 0, 16);
         icons[i] = createImage(mis);

         // Grab mask data
         maskData = new byte[256];
         for ( j = 0; j < 32; j++)
            for ( b = 0; b < 8; b++)
               maskData[j*8+b] = (byte)((rawData[j+32] &
                                        (0x80 >>> b)) > 0? 0x0F: 0x00);

         mis = new MemoryImageSource(16, 16, icm, maskData, 0, 16);
         masks[i] = createImage(mis);

	 // Could use DataInputStream - but just 2 bytes
         y[i] = rawData[64] << 8;
         y[i] |= rawData[65];
         y[i] &= 0x00FF;


         x[i] = rawData[66] << 8;
         x[i] |= rawData[67];
         x[i] &= 0x00FF;
      }
   }

   /*--------------------------------------------------------------------*/
   public void display()
   {
      TableColumn tc;
      TableCellRenderer renderer = new IconRenderer();
      DefaultTableModel tmpModel;

      Resource myResArray[] = resData.getResArray();

      setLayout(new BorderLayout());

      tmpModel = new DefaultTableModel(columnNames, myResArray.length);

      for( int i = 0; i < myResArray.length; i++)
      {
         tmpModel.setValueAt(new Short(myResArray[i].getID()),  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);
         tmpModel.setValueAt(icons[i], i, 3);
         tmpModel.setValueAt(masks[i], i, 4);
         tmpModel.setValueAt(new Integer(y[i]), i, 6);
         tmpModel.setValueAt(new Integer(x[i]), i, 5);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(20);

      addDecorator();

      tc = resTable.getColumn("Icon");
      tc.setCellRenderer(renderer);

      tc = resTable.getColumn("Mask");
      tc.setCellRenderer(renderer);

      optimizeColumnWidth();

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   public void save (  File savedir )
   {
      StringBuffer tmpfilename;
      String filename, imgname;
      String saveType;

      FileWriter fw;

      XpmImage xpmout;

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }
      Resource myResArray[] = resData.getResArray();
      saveType = resData.getID();

      System.out.println("Saving resources of type \'" + saveType + "\'");

      // Icons
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer("" + myResArray[i].getID() );
         if(myResArray[i].getName() != null)
         {
            imgname = myResArray[i].getName();
            tmpfilename.append("_" + myResArray[i].getName());
         } else
            imgname = "icon" + myResArray[i].getID();
         tmpfilename.append(".xpm");

         filename = tmpfilename.toString().
                replace(' ', '_').
                replace(File.separatorChar, '+');
         imgname = imgname.replace(' ', '_');

         try
         {
            fw = new FileWriter(new File(savedir, filename));
            if(icons[i] == null)
               System.err.println("Image " + imgname + " seems to be null!");
            else
            {
               if(masks[i] == null)
                  xpmout = new XpmImage(imgname, this, icons[i]);
               else
                  xpmout = new XpmImage(imgname, this, icons[i], masks[i]);
               xpmout.write(fw);
            }
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
}
