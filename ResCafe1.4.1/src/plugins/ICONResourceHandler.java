/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/ICONResourceHandler.java,v 1.5 2000/05/24 07:28:50 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Image;

import java.awt.image.*;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: ICONResourceHandler.java,v $
 * Revision 1.5  2000/05/24 07:28:50  gbsmith
 * Added calls to 'addDecorator'and 'optimizeColumnWidth' to use
 * that functionality.
 *
 * Revision 1.4  1999/10/21 21:28:35  gbsmith
 * Changed color model call.
 *
 * Revision 1.3  1999/10/21 21:26:50  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.2  1999/10/04 22:02:08  gbsmith
 * Adapted to new init technique.
 *
 * Revision 1.1  1999/09/30 05:21:43  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
public class ICONResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   private static final String[] columnNames = { "ResID", "Name", "Size", "Image"};
   IndexColorModel icm;
   TableCellRenderer renderer = new IconRenderer();

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: ICONResourceHandler.java,v 1.5 2000/05/24 07:28:50 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"ICON"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      int i, j, b;
      MemoryImageSource mis;
      byte rawData[];
      byte newData[];

      Resource myResArray[] = resData.getResArray();

      icm = MacStandard256Palette.getColorModel();

      myimages = new Image[myResArray.length];

      for( i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();
         newData = new byte[1024];
         for ( j = 0; j < 128; j++)
            for ( b = 0; b < 8; b++)
               newData[j*8+b] = (byte)((rawData[j] &
                                        (0x80 >>> b)) > 0? 0xFF: 0x00);

         mis = new MemoryImageSource(32, 32, icm, newData, 0, 32);
         myimages[i] = createImage(mis);
      }

   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int i;
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
         tmpModel.setValueAt(myimages[i], i, 3);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(36);

      addDecorator();

      tc = resTable.getColumn("Image");
      tc.setCellRenderer(renderer);

      optimizeColumnWidth();

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }
}
