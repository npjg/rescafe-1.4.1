/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/STR_ResourceHandler.java,v 1.5 2000/05/24 06:27:07 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: STR_ResourceHandler.java,v $
 * Revision 1.5  2000/05/24 06:27:07  gbsmith
 * Now subclasses DefaultResourceHandler to take advantage of the
 * column sorting and sizing capabilities. Also made export
 * filename construction changes to prevent file separator chars
 * from causing save errors.
 *
 * Revision 1.4  2000/05/24 03:55:25  gbsmith
 * Changed file/path name char replacement sequence
 *
 * Revision 1.3  1999/10/21 21:50:28  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.2  1999/10/04 22:10:58  gbsmith
 * Adapted to new init technique and added save method.
 *
 * Revision 1.1  1999/09/30 05:24:34  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
public class STR_ResourceHandler extends DefaultResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   String mystrings[];
   private static final String[] columnNames = { "ResID", "Name", "Size", "Text" };

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: STR_ResourceHandler.java,v 1.5 2000/05/24 06:27:07 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"STR ", "STR#", "TEXT"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      int i;
      byte rawData[];

      Resource myResArray[] = resData.getResArray();

      mystrings = new String[myResArray.length];

      for( i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();
         mystrings[i] = new String(rawData);
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int i;
      DefaultTableModel tmpModel;
      Resource myResArray[] = resData.getResArray();

      setLayout(new BorderLayout());
      tmpModel = new DefaultTableModel(columnNames, myResArray.length);

      for( i = 0; i < myResArray.length; i++)
      {
         tmpModel.setValueAt(new Short(myResArray[i].getID()),  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);
         tmpModel.setValueAt(mystrings[i], i, 3);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(18);

      addDecorator();
      optimizeColumnWidth();

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }


   /*--------------------------------------------------------------------*/
   public void save ( File savedir )
   {
      StringBuffer tmpfilename;
      String filename;
      String saveType;

      PrintWriter pw = null;

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      Resource myResArray[] = resData.getResArray();
      saveType = resData.getID();

      System.out.println("Saving resources of type \'" + saveType + "\'");
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer(savedir.getPath());
         tmpfilename.append( File.separator + myResArray[i].getID() );
         if(myResArray[i].getName() != null)
            tmpfilename.append("_" + myResArray[i].getName().
                               replace(' ', '_').
                               replace(File.separatorChar, '+'));
         tmpfilename.append(".txt");
         filename = tmpfilename.toString();

         try
         {
            pw = new PrintWriter(new FileOutputStream(filename));
            pw.print(mystrings[i]);
            pw.close();
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
}

