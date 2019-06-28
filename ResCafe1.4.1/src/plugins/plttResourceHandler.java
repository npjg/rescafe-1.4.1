/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/plttResourceHandler.java,v 1.2 1999/10/21 22:20:21 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: plttResourceHandler.java,v $
 * Revision 1.2  1999/10/21 22:20:21  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.1  1999/10/04 22:35:42  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class plttResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   Palette myPalettes[];

   JList resList;
   JTable resTable;
   private static final String[] columnNames = {
      "ResID", "Name", "Size", "#Colors" };

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: plttResourceHandler.java,v 1.2 1999/10/21 22:20:21 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"pltt"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      byte rawData[];

      Resource myResArray[] = resData.getResArray();
      myPalettes = new Palette[myResArray.length];


      for( int i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();
         myPalettes[i] = new Palette();
         myPalettes[i].parse(rawData);
      }
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      DefaultTableModel tmpModel;
      Resource myResArray[];

      setLayout(new BorderLayout());
      myResArray = resData.getResArray();
      tmpModel   = new DefaultTableModel(columnNames, myResArray.length);

      for( int i = 0; i < myResArray.length; i++)
      {
         // "ResID", "Name", "Size", "#Colors"
         tmpModel.setValueAt(new Short(myResArray[i].getID()),     i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),              i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()),    i, 2);
         tmpModel.setValueAt(new Integer(myPalettes[i].numColors), i, 3);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(18);

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }


   /*--------------------------------------------------------------------*/
   public void save ( File savedir )
   {
      StringBuffer tmpfilename, tmpclassname;
      String classname, filename;
      String saveType;
      Resource myResArray[];

      if(resData == null)
      {
         System.err.println("ERROR: No resources of to save");
         return;
      }

      myResArray = resData.getResArray();
      saveType = resData.getID();

      System.out.println("Saving resources of type \'" + saveType + "\' as java");
      for(int i=0; i < myResArray.length; i++)
      {
         tmpclassname = new StringBuffer("Mac_" + myResArray[i].getID());
         if(myResArray[i].getName() != null)
            tmpclassname.append("_" + myResArray[i].getName());

         classname = tmpclassname.toString().replace(' ', '_');

         tmpfilename = new StringBuffer(savedir.getPath());
         tmpfilename.append( File.separator + classname );
         tmpfilename.append(".java");
         filename = tmpfilename.toString().replace(' ', '_'); // Redundant?
         //System.out.println("\tSaving \'" + filename + "\'...");

         try
         {
            PrintWriter saveps = new PrintWriter(new FileOutputStream(filename));
            saveps.println("class " + classname);
            saveps.println("{");
            myPalettes[i].printJava(saveps);
            saveps.println("");
            saveps.println("}");
            saveps.close();
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
}


