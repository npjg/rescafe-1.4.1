/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/DefaultResourceHandler.java,v 1.6 2000/11/27 19:57:24 gbsmith Exp $ */

import javax.swing.*;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import javax.swing.table.*;

import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.FileOutputStream;

import java.util.Enumeration;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: DefaultResourceHandler.java,v $
 * Revision 1.6  2000/11/27 19:57:24  gbsmith
 * Added ident tag to SortDecorator
 *
 * Revision 1.5  2000/11/25 07:02:39  gbsmith
 * Added some null tests in columnHeaderWidth() which was crashing
 * under Java 1.3.
 *
 * Revision 1.4  2000/05/24 06:13:45  gbsmith
 * Moved column width optimization and column sorting functionality
 * in from IconFamily so it can be exploited by all subclass (and
 * used for unhandled types). Also added copyright notice.
 *
 * Revision 1.3  1999/10/13 23:35:14  gbsmith
 * Modified variable names to match updated MacResourceHandler superclass.
 * Also removed raw byte save method since it is now inherited from
 * MacResourceHandler.
 *
 * Revision 1.2  1999/10/04 20:54:27  gbsmith
 * Changed init method to reflect new method of passing data to Handler.
 * Added raw data saving method.
 *
 * Revision 1.1  1999/09/30 05:21:23  gbsmith
 * Initial revision
 */


/*=======================================================================*/
public class DefaultResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   protected JList  resList;
   protected JTable resTable;
   protected SortDecorator decorator;
   protected static final String[] columnNames = { "ResID", "Name", "Size"};

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: DefaultResourceHandler.java,v 1.6 2000/11/27 19:57:24 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes( )
   {
      return new String[]{"default"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
   }

   /*--------------------------------------------------------------------*/
   public void display( )
   {
      DefaultTableModel tmpModel;
      Enumeration resIDs;
      Resource currentRes;

      setLayout(new BorderLayout());
      tmpModel = new DefaultTableModel(columnNames, resData.size());

      int i = 0;
      resIDs = resData.getResourceIDs();
      while(resIDs.hasMoreElements())
      {
         currentRes = resData.getResource((Short)resIDs.nextElement());
         tmpModel.setValueAt(new Short(currentRes.getID()),  i, 0);
         tmpModel.setValueAt(currentRes.getName(),           i, 1);
         tmpModel.setValueAt(new Integer(currentRes.size()), i, 2);
         i++;
      }

      resTable = new JTable(tmpModel);

      addDecorator(); 
      optimizeColumnWidth();

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }


   /*--------------------------------------------------------------------*/
   protected void addDecorator()
   {
      JTableHeader hdr = (JTableHeader)resTable.getTableHeader();

      decorator = new SortDecorator(resTable.getModel());
      resTable.setModel(decorator);

      hdr.addMouseListener(
         new MouseAdapter()
            {
               public void mouseClicked(MouseEvent e)
               {
                  TableColumnModel tcm = resTable.getColumnModel();
                  int vc = tcm.getColumnIndexAtX(e.getX());
                  int mc = resTable.convertColumnIndexToModel(vc);
                  decorator.sort(mc);
               }
            }
         );
   }


   /*>>>>>>>>>>>>>>>>>>>>> BEGIN Column fitting code <<<<<<<<<<<<<<<<<<<<*/
   /*
     Based on code from David Geary's _Graphic Java 2: Mastering the JFC
     Prentice Hall 1999
    */
   /*--------------------------------------------------------------------*/
   protected void optimizeColumnWidth()
   {
      TableColumn currentCol;
      int currentWidth;

      for(int c = 0; c < columnNames.length; c++)
      {
         currentCol = resTable.getColumn(columnNames[c]);
         currentWidth = getPreferredWidthForColumn(currentCol) + 5;
         currentCol.setMinWidth(currentWidth);
      }
      // Tighten up 1st column
      currentCol   = resTable.getColumn(columnNames[0]);
      currentWidth = getPreferredWidthForColumn(currentCol) + 5;
      currentCol.setMaxWidth(currentWidth);

      resTable.sizeColumnsToFit(0);
   }

   /*--------------------------------------------------------------------*/
   protected int getPreferredWidthForColumn(TableColumn inCol)
   {
      int headerWidth = columnHeaderWidth(inCol);
      int columnWidth = widestCellInColumn(inCol);

      return headerWidth > columnWidth ? headerWidth : columnWidth;
   }


   /*--------------------------------------------------------------------*/
   protected int columnHeaderWidth(TableColumn inCol)
   {
      TableCellRenderer renderer = inCol.getHeaderRenderer();
      if(renderer == null) return 0; // no renderer? can't ask it anything

      Component comp = renderer.getTableCellRendererComponent(
         resTable, inCol.getHeaderValue(), false, false, 0, 0);
      if(comp == null) return 0; // no comp -> no size

      return comp.getPreferredSize().width;
   }

   /*--------------------------------------------------------------------*/
   protected int widestCellInColumn(TableColumn inCol)
   {
      int col = inCol.getModelIndex(), width=0, maxw=0;

      for(int row=0; row < resTable.getRowCount(); ++row)
      {
         TableCellRenderer renderer = resTable.getCellRenderer(row, col);

         Component comp = renderer.getTableCellRendererComponent(
            resTable, resTable.getValueAt(row, col), false, false, row, col);

         width = comp.getPreferredSize().width;
         maxw = width > maxw ? width : maxw;
      }

      return maxw;
   }
   /*>>>>>>>>>>>>>>>>>>>>>> END Column fitting code <<<<<<<<<<<<<<<<<<<<<*/
}


/*=======================================================================*/
class SortDecorator implements TableModel, TableModelListener 
{
   private TableModel trueModel;
   private int indices[];

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: DefaultResourceHandler.java,v 1.6 2000/11/27 19:57:24 gbsmith Exp $";

   /*--------------------------------------------------------------------*/
   public SortDecorator(TableModel model) 
   {
      if(model == null) 
         throw new IllegalArgumentException( "null models are not allowed");
      trueModel = model;	

      trueModel.addTableModelListener(this);
      makeArray();
   }

   /*--------------------------------------------------------------------*/
   public Object getValueAt(int row, int column) 
   {
      return trueModel.getValueAt(indices[row], column);
   }

   /*--------------------------------------------------------------------*/
   public void setValueAt(Object inVal, int row, int column) 
   {
      trueModel.setValueAt(inVal, indices[row], column);
   }

   /*--------------------------------------------------------------------*/
   public void tableChanged(TableModelEvent e) 
   {
      makeArray();
   }

   /*--------------------------------------------------------------------*/
   public void sort(int column) 
   {
      int rowCount = getRowCount();

      for(int i=0; i < rowCount; i++)
         for(int j = i+1; j < rowCount; j++)
            if(compare(indices[i], indices[j], column) < 0)
               swap(i,j);
   }

   /*--------------------------------------------------------------------*/
   public void swap(int i, int j) 
   {
      int temp   = indices[i];
      indices[i] = indices[j];
      indices[j] = temp;
   }

   /*--------------------------------------------------------------------*/
   public int compare(int i, int j, int column) 
   {
      Object iObj = trueModel.getValueAt(i,column);
      Object jObj = trueModel.getValueAt(j,column);

      if(iObj == null) return  1; // Handle nulls
      if(jObj == null) return -1;

      int comp = jObj.toString().compareTo(iObj.toString());

      if (comp < 0)      return -1;
      else if (comp > 0) return  1;
      else               return  0;
   }

   /*--------------------------------------------------------------------*/
   private void makeArray() 
   {
      indices = new int[getRowCount()];
      for(int i=0; i < indices.length; ++i)
         indices[i] = i;
   }

   /*--------------------------------------------------------------------*/
   // TableModel pass-through 
   /*--------------------------------------------------------------------*/
   public int getRowCount() {
      return trueModel.getRowCount();	
   }
   public int getColumnCount() {
      return trueModel.getColumnCount();	
   }
   public String getColumnName(int columnIndex) {
      return trueModel.getColumnName(columnIndex);
   }
   public Class getColumnClass(int columnIndex) {
      return trueModel.getColumnClass(columnIndex);
   }
   public boolean isCellEditable(int rowIndex, int columnIndex) {
      return trueModel.isCellEditable(rowIndex, columnIndex);
   }
   public void addTableModelListener(TableModelListener l) {
      trueModel.addTableModelListener(l);
   }
   public void removeTableModelListener(TableModelListener l) {
      trueModel.removeTableModelListener(l);
   }
   /*--------------------------------------------------------------------*/
}

