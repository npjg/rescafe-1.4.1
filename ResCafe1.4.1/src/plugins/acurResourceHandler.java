/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/acurResourceHandler.java,v 1.3 1999/10/21 21:54:39 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Image;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: acurResourceHandler.java,v $
 * Revision 1.3  1999/10/21 21:54:39  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.2  1999/10/04 22:12:46  gbsmith
 * Adapted to new init technique.
 *
 * Revision 1.1  1999/09/30 05:31:01  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class acurResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   int numFrames[];
   JList resList;
   JTable resTable;
   private static final String[] columnNames = {
      "ResID", "Name", "Size", "#Frames", "Counter", "First","Last"  };

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: acurResourceHandler.java,v 1.3 1999/10/21 21:54:39 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"acur"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      int i, j, b;
      byte rawData[];

      Resource myResArray[] = resData.getResArray();
      numFrames = new int[myResArray.length];

      System.err.println("NOTE: This plugin is incomplete");
      for( i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();

         // Grab just the number of frames for now
         numFrames[i] = rawData[0] << 8;
         numFrames[i] |= rawData[1];
         numFrames[i] &= 0x00FF;
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int i, j, b;
      byte rawData[];
      DefaultTableModel tmpModel;

      Resource myResArray[] = resData.getResArray();

      setLayout(new BorderLayout());
      tmpModel = new DefaultTableModel(columnNames, myResArray.length);

      System.err.println("NOTE: This plugin is incomplete");

      for( i = 0; i < myResArray.length; i++)
      {
         tmpModel.setValueAt(new Short(myResArray[i].getID()),  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);
         tmpModel.setValueAt(new Integer(numFrames[i]), i, 3);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(18);

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }
}
