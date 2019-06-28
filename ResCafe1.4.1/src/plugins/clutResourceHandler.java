/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/clutResourceHandler.java,v 1.4 1999/10/22 04:09:24 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: clutResourceHandler.java,v $
 * Revision 1.4  1999/10/22 04:09:24  gbsmith
 * Added some class imports.
 *
 * Revision 1.3  1999/10/21 21:56:39  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.2  1999/10/04 22:14:02  gbsmith
 * Adapted to new init technique.
 *
 * Revision 1.1  1999/09/30 05:31:32  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class clutResourceHandler extends DefaultResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   int numColors[];
   JList resList;
   JTable resTable;
   private static final String[] columnNames = { "ResID", "Name", "Size", "#Colors" };

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: clutResourceHandler.java,v 1.4 1999/10/22 04:09:24 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"clut"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      byte rawData[];
      Resource myResArray[] = resData.getResArray();

      numColors = new int[myResArray.length];

      System.err.println("NOTE: This plugin is incomplete");

      for( int i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();

         // Grab just the number of colors for now
         numColors[i] = rawData[6] << 8;
         numColors[i] |= rawData[7];
         numColors[i] &= 0x00FF;
         numColors[i]++;
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      Resource myResArray[] = resData.getResArray();
      DefaultTableModel tmpModel;

      setLayout(new BorderLayout());

      tmpModel = new DefaultTableModel(columnNames, myResArray.length);

      // "ResID", "Name", "Size", "#Colors"
      for( int i = 0; i < myResArray.length; i++)
      {
         tmpModel.setValueAt(new Short(myResArray[i].getID()),  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);
         tmpModel.setValueAt(new Integer(numColors[i]),         i, 3);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(18);

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }
}
