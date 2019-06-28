/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/cicnResourceHandler.java,v 1.4 2000/05/25 07:44:44 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: cicnResourceHandler.java,v $
 * Revision 1.4  2000/05/25 07:44:44  gbsmith
 * Switched from Jimi to XpmImage which means proper handling
 * of masks.
 *
 * Revision 1.3  1999/10/21 22:29:04  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.2  1999/10/17 23:10:16  gbsmith
 * Made class imports more explicit.
 * Some plain reformatting and reindentation.
 *
 * Revision 1.1  1999/10/17 19:37:43  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
public class cicnResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   private static final String[] columnNames = {
      "ResID", "Name", "Size", "Icon", "Bitmap", "Mask" };

   Image icons[];
   Image bitmaps[];
   Image masks[];

   TableCellRenderer renderer = new IconRenderer();
   cicnParser mycp;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: cicnResourceHandler.java,v 1.4 2000/05/25 07:44:44 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"cicn"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      int i, j, b;
      mycp = new cicnParser();
      ByteArrayInputStream bais;
      byte rawData[];

      Resource myResArray[] = resData.getResArray();

      icons = new Image[myResArray.length];
      bitmaps = new Image[myResArray.length];
      masks = new Image[myResArray.length];

      for( i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();
         bais = new ByteArrayInputStream(rawData);
         try
         {
            mycp.read(bais);
            bitmaps[i] = mycp.getBitmap();
            masks[i]   = mycp.getMask();
            icons[i]   = mycp.getIcon();
         } catch (Exception ignore) {}
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int i, j, b;
      TableColumn tc;
      DefaultTableModel tmpModel;

      Resource myResArray[] = resData.getResArray();

      setLayout(new BorderLayout());
      tmpModel = new DefaultTableModel(columnNames, myResArray.length);

      for( i = 0; i < myResArray.length; i++)
      {
         tmpModel.setValueAt(new Short(myResArray[i].getID()),  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);

         tmpModel.setValueAt(icons[i],   i, 3);
         tmpModel.setValueAt(bitmaps[i], i, 4);
         tmpModel.setValueAt(masks[i],   i, 5);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(36);

      addDecorator();

      tc = resTable.getColumn("Icon");
      tc.setCellRenderer(renderer);

      tc = resTable.getColumn("Bitmap");
      tc.setCellRenderer(renderer);

      tc = resTable.getColumn("Mask");
      tc.setCellRenderer(renderer);

      optimizeColumnWidth();

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   public void save ( File savedir )
   {
      StringBuffer tmpfilename;
      String filename, imgname, saveType;

      File outfile;
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
         tmpfilename = new StringBuffer( "" + myResArray[i].getID() );

         if(myResArray[i].getName() != null)
         {
            imgname = myResArray[i].getName();
            tmpfilename.append("_" + myResArray[i].getName());
         } else
            imgname = "icon" + myResArray[i].getID();
         tmpfilename.append(".xpm");

         filename = tmpfilename.toString().replace(' ', '_').
            replace(File.separatorChar, '+');
         imgname = imgname.replace(' ', '_');

         try
         {
            if(icons[i] == null)
               System.err.println("Image " + imgname + " seems to be null!");
            else
            {
               fw = new FileWriter(new File(savedir, filename));
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


      // Bitmaps
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer("" + myResArray[i].getID() );
         
         if(myResArray[i].getName() != null)
         {
            imgname = myResArray[i].getName();
            tmpfilename.append("_" + myResArray[i].getName());
         } else
            imgname = "bitmap" + myResArray[i].getID();
         tmpfilename.append("_bitmap.xpm");

         filename = tmpfilename.toString().replace(' ', '_').
            replace(File.separatorChar, '+');
         imgname = imgname.replace(' ', '_');

         try
         {
            if(bitmaps[i] == null)
               System.err.println("Image " + imgname + " seems to be null!");
            else
            {
               fw = new FileWriter(new File(savedir, filename));
               if(masks[i] == null)
                  xpmout = new XpmImage(imgname, this, bitmaps[i]);
               else
                  xpmout = new XpmImage(imgname, this, bitmaps[i], masks[i]);
               xpmout.write(fw);
            }
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
}
