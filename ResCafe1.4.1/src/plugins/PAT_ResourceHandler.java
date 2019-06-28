/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/PAT_ResourceHandler.java,v 1.2 1999/10/21 21:45:20 gbsmith Exp $ */

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

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: PAT_ResourceHandler.java,v $
 * Revision 1.2  1999/10/21 21:45:20  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 * Changed color model call.
 *
 * Revision 1.1  1999/10/04 22:03:04  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class PAT_ResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   IndexColorModel icm;
   private static final String[] columnNames = { "ResID", "Name", "Size", "Pattern"};
   JList resList;
   JTable resTable;
   TableCellRenderer renderer = new IconRenderer();

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: PAT_ResourceHandler.java,v 1.2 1999/10/21 21:45:20 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"PAT "};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int i, j, b;
      MemoryImageSource mis;
      TableColumn tc;
      byte rawData[];
      byte newData[];

      Resource myResArray[] = resData.getResArray();

      setLayout(new BorderLayout());
      icm = MacStandard16Palette.getColorModel();

      myimages = new Image[myResArray.length];

      DefaultTableModel tmpModel =
         new DefaultTableModel(columnNames, myResArray.length);

      for( i = 0; i < myResArray.length; i++)
      {
         tmpModel.setValueAt(new Short(myResArray[i].getID()),  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);

         rawData = myResArray[i].getData();
         newData = new byte[64];
         for ( j = 0; j < 8; j++)
            for ( b = 0; b < 8; b++)
               newData[j*8+b] = (byte)((rawData[j] &
                                        (0x80 >>> b)) > 0? 0xFF: 0x00);

         mis = new MemoryImageSource(8, 8, icm, newData, 0, 8);
         myimages[i] = createImage(mis);
         tmpModel.setValueAt(myimages[i], i, 3);

      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(16);
      tc = resTable.getColumn("Pattern");
      tc.setCellRenderer(renderer);

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }
}
