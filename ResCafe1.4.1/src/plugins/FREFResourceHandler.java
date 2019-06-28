/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/FREFResourceHandler.java,v 1.2 1999/10/21 21:18:13 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Enumeration;

import ResourceManager.*;


/*=======================================================================*/
/*
 * $Log: FREFResourceHandler.java,v $
 * Revision 1.2  1999/10/21 21:18:13  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.1  1999/10/04 21:56:32  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class FREFResourceHandler extends DefaultResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   JList resList;
   JTable resTable;
   private static final String[] columnNames = {
      "ResID", "Name", "Size", "File type", "Local Icon ID" };
   //      0       1       2            3                4

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: FREFResourceHandler.java,v 1.2 1999/10/21 21:18:13 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"FREF"};
   }

   /*--------------------------------------------------------------------*/
   public void init(String inType, ResourceType inResData )
   {
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      byte rawData[];
      DefaultTableModel tmpModel;
      Enumeration resIDs;
      Resource currentRes;
      DataInputStream dis;

      // Fields
      String fileType = null;
      short local_icon_ID = 0;

      setLayout(new BorderLayout());
      tmpModel = new DefaultTableModel(columnNames, resData.size());

      System.err.println("NOTE: This plugin is incomplete");

      int i = 0;
      resIDs = resData.getResourceIDs();
      while(resIDs.hasMoreElements())
      {
         currentRes = resData.getResource((Short)resIDs.nextElement());

         rawData = currentRes.getData();
         dis = new DataInputStream(new ByteArrayInputStream(rawData));

         // Fields
         fileType = new String(rawData, 0, 4);

         local_icon_ID  = 0;
         local_icon_ID  = (short)(rawData[4] << 8);
         local_icon_ID |= (short)(rawData[5] & 0x00FF);

         // Set fields
         // "ResID", "Name", "Size", "File type", "Local Icon ID" };
         //      0       1       2            3                4
         tmpModel.setValueAt(new Short(currentRes.getID()),  i, 0);
         tmpModel.setValueAt(currentRes.getName(),           i, 1);
         tmpModel.setValueAt(new Integer(currentRes.size()), i, 2);
         tmpModel.setValueAt(fileType,                       i, 3);
         tmpModel.setValueAt(new Integer(local_icon_ID),     i, 4);

         i++;
      }

      resTable = new JTable(tmpModel);
      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   // public void save ( File savedir )
   // { // Saves as bytes
   // }
}
