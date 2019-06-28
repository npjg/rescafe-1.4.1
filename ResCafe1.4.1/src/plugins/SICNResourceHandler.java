/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/SICNResourceHandler.java,v 1.4 1999/12/19 05:34:51 gbsmith Exp $ */

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.*;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: SICNResourceHandler.java,v $
 * Revision 1.4  1999/12/19 05:34:51  gbsmith
 * Now uses inherited process_ics method for grabbing
 *
 * Revision 1.3  1999/10/21 21:48:10  gbsmith
 * Added Copyright notice. Changed color model call.
 *
 * Revision 1.2  1999/10/04 22:07:04  gbsmith
 * Adapted to new init technique.
 *
 * Revision 1.1  1999/09/30 05:22:52  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class SICNResourceHandler extends GBS_ImageResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   JList resList;
   JTable resTable;
   private static final String[] columnNames = { "ResID", "Name", "Size", "Icon" };

   TableCellRenderer renderer = new IconRenderer();

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: SICNResourceHandler.java,v 1.4 1999/12/19 05:34:51 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"SICN"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      int i;

      Resource myResArray[] = resData.getResArray();

      myimages = new Image[myResArray.length];
      for( i = 0; i < myResArray.length; i++)
         myimages[i] =  process_ics( myResArray[i].getData() );
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
         // "ResID", "Name", "Size", "Icon"
         tmpModel.setValueAt(new Short(myResArray[i].getID()),  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);
         tmpModel.setValueAt(myimages[i],                       i, 3);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(20);
      tc = resTable.getColumn("Icon");
      tc.setCellRenderer(renderer);

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }
}
