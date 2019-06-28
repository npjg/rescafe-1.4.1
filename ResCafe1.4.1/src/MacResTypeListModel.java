/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/MacResTypeListModel.java,v 1.3 1999/10/21 22:59:37 gbsmith Exp $ */

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import javax.swing.border.Border;

import java.awt.Component;
import java.awt.Color;

import java.util.Hashtable;

/*=======================================================================*/
/*
 * $Log: MacResTypeListModel.java,v $
 * Revision 1.3  1999/10/21 22:59:37  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.2  1999/10/04 21:18:13  gbsmith
 * Added comments. Changed border.
 *
 * Revision 1.1  1999/09/30 05:23:14  gbsmith
 * Initial revision
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
class MacResTypeListModel extends DefaultListModel
{
   /*--- Data -----------------------------------------------------------*/
   Hashtable type_icons;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: MacResTypeListModel.java,v 1.3 1999/10/21 22:59:37 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public MacResTypeListModel(Hashtable inIcons)
   {
      super();
      type_icons = inIcons;
   }

   /*--------------------------------------------------------------------*/
   public void addInOrder(String newItem)
   {
      String current;
      int u_bound, l_bound, i;

      l_bound = 0;
      u_bound = size()-1;

      // Figure out where the new item should go
      while (u_bound >= l_bound)
      {
         i = l_bound + (u_bound - l_bound) / 2;
         current = (String)getName(getElementAt(i));
         if (current.compareTo(newItem) < 0)
         {
            l_bound = i + 1;
         } else {
            u_bound = i - 1;
         }
      }

      // Now actually add it
      if( type_icons.containsKey(newItem) )
         add( l_bound, new Object[]{ newItem, type_icons.get(newItem)});
      else
         add( l_bound, new Object[]{ newItem, type_icons.get("default")});
   }

   /*--------------------------------------------------------------------*/
   public String getName(Object object)
   {
      Object[] array = (Object[])object;
      return (String)array[0];
   }

   /*--------------------------------------------------------------------*/
   public Icon getIcon(Object object)
   {
      Object[] array = (Object[])object;
      return (Icon)array[1];
   }

   /*--------------------------------------------------------------------*/
   public boolean contains(String inStr)
   {
      int total = size();

      for(int i = 0; i < total; i++)
         if(inStr.equals(getElementAt(i)))
            return true;

      return false;
   }

   /*--------------------------------------------------------------------*/
   public void setItems( String[] inItems )
   {
      removeAllElements();
      for(int i = 0; i < inItems.length; i++)
         addElement(inItems[i]);
   }
}


/*=======================================================================*/
class MacResTypeListCellRenderer extends JLabel implements ListCellRenderer
{
   /*--- Data -----------------------------------------------------------*/
   private Border lineBorder = BorderFactory.createLineBorder(Color.black, 2),
      emptyBorder = BorderFactory.createEmptyBorder(2,2,2,2);

   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: MacResTypeListModel.java,v 1.3 1999/10/21 22:59:37 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public MacResTypeListCellRenderer()
   {
      setOpaque(true);
   }

   /*--------------------------------------------------------------------*/
   public Component getListCellRendererComponent(
      JList list,
      Object value,
      int index,
      boolean isSelected,
      boolean cellHasFocus)
   {
      MacResTypeListModel model = (MacResTypeListModel)list.getModel();

      setText(model.getName(value));
      setIcon(model.getIcon(value));

      if(isSelected)
      {
         setForeground(list.getSelectionForeground());
         setBackground(list.getSelectionBackground());
      } else {
         setForeground(list.getForeground());
         setBackground(list.getBackground());
      }

      if(cellHasFocus) setBorder(lineBorder);
      else 	       setBorder(emptyBorder);

      return this;
   }
}
