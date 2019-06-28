/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/IconRenderer.java,v 1.6 1999/10/21 21:37:20 gbsmith Exp $ */

import java.awt.Component;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;

import javax.swing.table.DefaultTableCellRenderer;

/*=======================================================================*/
/*
 * $Log: IconRenderer.java,v $
 * Revision 1.6  1999/10/21 21:37:20  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.5  1999/10/17 20:20:47  gbsmith
 * Added type checks to allow rendering of non-Image data as well.
 * This allows for messages to be displayed when no Image was found.
 *
 * Revision 1.4  1999/10/16 02:50:22  gbsmith
 * Return Default Renderer in the case of no Icon - previous version had the
 * last Icon carrying over to the next cell
 *
 * Revision 1.3  1999/10/16 02:35:05  gbsmith
 * Added a null check to make sure we don't try and render non-existent
 * icons.
 *
 * Revision 1.2  1999/10/04 21:16:28  gbsmith
 * Added some comments
 *
 * Revision 1.1  1999/09/30 05:22:43  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class IconRenderer extends DefaultTableCellRenderer
{
   /*--- Data -----------------------------------------------------------*/
   private ImageIcon theIcon;

   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: IconRenderer.java,v 1.6 1999/10/21 21:37:20 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public IconRenderer()
   {
      setHorizontalAlignment(JLabel.CENTER);
   }

   /*--------------------------------------------------------------------*/
   public Component getTableCellRendererComponent(JTable table, Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int row, int col)
   {
      // Make sure we actually have icon data to work with
      if(value != null)
      {
         if(value instanceof Image)
         {
            theIcon = new ImageIcon((Image)value);
            setIcon(theIcon);
            return this;
         }

         // Not an image
         DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
         dtcr.setText(value.toString());
         dtcr.setHorizontalAlignment(JLabel.CENTER);
         return dtcr;
      }

      // Nothing to show
      return new DefaultTableCellRenderer();
   }
}
