/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/MENUResourceHandler.java,v 1.2 2000/11/27 20:14:33 gbsmith Exp $ */

/*=======================================================================*/
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.Hashtable;

import ResourceManager.*;

/*=======================================================================*/
/*
 * $Log: MENUResourceHandler.java,v $
 * Revision 1.2  2000/11/27 20:14:33  gbsmith
 * Uses Unicode \u00c9 for capital E aigu. Added RCS ident tag to
 * RowController inner class.
 *
 * Revision 1.1  1999/10/21 21:40:41  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class MENUResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   //MENURecord mymenus[];
   Hashtable mymenus;

   JList resList;
   JTable resTable;
   JMenuBar demobar;
   JMenu currentMenu;

   private static final String[] columnNames = { "ResID", "Name", "Size", "Menu Title" };

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: MENUResourceHandler.java,v 1.2 2000/11/27 20:14:33 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"MENU"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
   {
      readMENUs();
   }

   /*--------------------------------------------------------------------*/
   public void readMENUs()
   {
      int i;
      byte rawData[];
      DataInputStream dis;
      MENURecord tmpMR;

      Resource myResArray[] = resData.getResArray();

      //mymenus = new MENURecord[myResArray.length];
      mymenus = new Hashtable();
      currentMenu = null;

      for( i = 0; i < myResArray.length; i++)
      {
         rawData = myResArray[i].getData();
         dis = new DataInputStream( new ByteArrayInputStream(rawData) );
         //mymenus[i] = new MENURecord();
         tmpMR = new MENURecord();

         try
         {
            tmpMR.read(dis);
         } catch(IOException ioe) {
            System.err.println("OOPS! " + ioe);
         }

         mymenus.put( new Short(myResArray[i].getID()), tmpMR);
      }
   }


   /*--------------------------------------------------------------------*/
   public void display( )
   {
      int i;
      DefaultTableModel tmpModel;
      Resource myResArray[] = resData.getResArray();
      Short resid;

      setLayout(new BorderLayout());
      tmpModel = new DefaultTableModel(columnNames, myResArray.length);

      demobar = new JMenuBar();
      demobar.add(new JMenu("MENU")); // Placeholder Menu
      add(demobar, "North");
      demobar.setVisible(true);

      for( i = 0; i < myResArray.length; i++)
      {
         resid = new Short(myResArray[i].getID());

         tmpModel.setValueAt(resid,  i, 0);
         tmpModel.setValueAt(myResArray[i].getName(),           i, 1);
         tmpModel.setValueAt(new Integer(myResArray[i].size()), i, 2);
         tmpModel.setValueAt(((MENURecord)mymenus.get(resid)).title, i, 3);
      }

      resTable = new JTable(tmpModel);
      resTable.setRowHeight(18);

      resTable.getSelectionModel().
         setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      resTable.getSelectionModel().
         addListSelectionListener(new RowController());

      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   private void updateDisplay( )
   {
      if(!resTable.getSelectionModel().isSelectionEmpty())
      {
         int index = resTable.getSelectedRow();
         Short idkey;
         MENURecord currentMR;

         idkey = (Short)resTable.getModel().getValueAt(index, 0);
         currentMR = (MENURecord)mymenus.get(idkey);

         // Put in Menubar
         demobar.removeAll();
         currentMenu = renderJMenu(currentMR);

         demobar.add(currentMenu);
         demobar.setVisible(false);
         demobar.setVisible(true);
      }
   }

   /*--------------------------------------------------------------------*/
   JMenu renderJMenu( MENURecord inMR )
   {
      Enumeration items;
      MenuItemRecord tmpMIR;
      Resource iconRes;
      ImageIcon menuIcon;
      JMenuItem jmi;

      // Submenu stuff
      Short subid;
      JMenu submenu;
      MENURecord subrec;

      JMenu outMenu = new JMenu(inMR.title);
      items = inMR.MenuItems.elements();

      while(items.hasMoreElements())
      {
         // This is all just a mess...
         tmpMIR = (MenuItemRecord)items.nextElement();
         if(tmpMIR.isSeparator)
            outMenu.addSeparator();
         else
         {
            String itemStr = tmpMIR.itemText;
            //System.out.println("Code: " + new String( new byte[]{ (byte)0xC9 } ) );
            //"É"
            if(itemStr.endsWith("\u00c9")) // i.e., É = captial E aigu
               itemStr = itemStr.substring(0, itemStr.length() - 1) + "...";

            if(tmpMIR.hasSubMenu)
            {
               subid = new Short(tmpMIR.mark_or_submenu);
               subrec = (MENURecord)mymenus.get(subid);
               outMenu.add(renderJMenu(subrec));
            } else {
               if(tmpMIR.hasIcon() )
               {
                  if(tmpMIR.useSICN)
                  {
                     iconRes = resMod.getResource("SICN", tmpMIR.getIcon());
                     menuIcon = getSICNImage( iconRes.getData() );
                     jmi = new JMenuItem(itemStr, menuIcon);
                  } else {
                     iconRes = resMod.getResource("ICON", tmpMIR.getIcon());
                     menuIcon = getICONImage( iconRes.getData(), tmpMIR.reduceIconSize );
                     jmi = new JMenuItem(itemStr, menuIcon);
                  }
               } else jmi = new JMenuItem(itemStr);

               if(tmpMIR.hasKeyEquiv) jmi.setMnemonic((char)tmpMIR.key_equiv);
               outMenu.add(jmi);
            }
         }
      }
      return outMenu;
   }

   /*--------------------------------------------------------------------*/
   ImageIcon getSICNImage( byte rawData[] )
   {
      byte iconData[];
      IndexColorModel icm = new IndexColorModel(8, 16,
                                                MacStandard16Palette.getReds(),
                                                MacStandard16Palette.getGreens(),
                                                MacStandard16Palette.getBlues());
      // Grab icon data
      iconData = new byte[256];
      for ( int j = 0; j < 32; j++)
         for ( int b = 0; b < 8; b++)
            iconData[j*8+b] = (byte)((rawData[j] &
                                      (0x80 >>> b)) > 0? 0x0F: 0x00);

      MemoryImageSource mis = new MemoryImageSource(16, 16, icm, iconData, 0, 16);
      return new ImageIcon(createImage(mis));
   }

   /*--------------------------------------------------------------------*/
   ImageIcon getICONImage( byte rawData[], boolean reduce )
   {
      Image tmpImg;
      byte iconData[];
      IndexColorModel icm = new IndexColorModel(8, 256,
                                                MacStandard256Palette.getReds(),
                                                MacStandard256Palette.getGreens(),
                                                MacStandard256Palette.getBlues());
      // Grab icon data
      iconData = new byte[1024];
      for ( int j = 0; j < 128; j++)
            for ( int b = 0; b < 8; b++)
               iconData[j*8+b] = (byte)((rawData[j] &
                                        (0x80 >>> b)) > 0? 0xFF: 0x00);

      MemoryImageSource mis = new MemoryImageSource(32, 32, icm, iconData, 0, 32);
      tmpImg = createImage(mis);
      if(reduce) tmpImg = tmpImg.getScaledInstance(16, 16, 0); // What are hints?

      return new ImageIcon(tmpImg);
   }



   /*====================================================================*/
   class RowController implements ListSelectionListener
   {
      /*------ RCS ---------------------------------------------------------*/
      static final String rcsid = "$Id: MENUResourceHandler.java,v 1.2 2000/11/27 20:14:33 gbsmith Exp $";

      /*-----------------------------------------------------------------*/
      public void valueChanged(ListSelectionEvent lse)
      {
         if(!lse.getValueIsAdjusting()) updateDisplay();
      }
   }

   /*--------------------------------------------------------------------*/
   /*
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
            tmpfilename.append("_" + myResArray[i].getName());
         tmpfilename.append(".txt");
         filename = tmpfilename.toString().replace(' ', '_');
         //System.out.println("\tSaving \'" + filename + "\'...");

         try
         {
            pw = new PrintWriter(new FileOutputStream(filename));
            //System.out.println("mystrings[" + i + "] = " + mystrings[i]);
            pw.print(mystrings[i]);
            pw.close();
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
   */
}

