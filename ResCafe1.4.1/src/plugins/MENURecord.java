/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/MENURecord.java,v 1.2 2000/11/27 20:12:13 gbsmith Exp $ */

import javax.swing.JMenu;

import java.io.DataInputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Vector;

/*=======================================================================*/
/*
 * $Log: MENURecord.java,v $
 * Revision 1.2  2000/11/27 20:12:13  gbsmith
 * Added RCS ident tag
 *
 * Revision 1.1  1999/10/21 21:38:58  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
class MENURecord
{
   /*--- Data -----------------------------------------------------------*/
   int MenuID;      // 2 bytes
   int MenuWidth;   // 2 bytes / These two
   int MenuHeight;  // 2 bytes / should be zero
   int MDEFResID;   // 2 bytes
   // PlaceHolder;  // 2 bytes
   int InitEnabledState; // 4 bytes - Used as a 32-bit array
   // title len     // 1 byte
   String title;

   Vector MenuItems; // var
   // PlaceHolder    // 1 byte - 0 indicates end of list

   /*--- RCS ------------------------------------------------------------*/
   static final String rcsid = "$Id: MENURecord.java,v 1.2 2000/11/27 20:12:13 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   MENURecord()
   {
      MenuItems = new Vector();
   }

   /*--------------------------------------------------------------------*/
   void read( DataInputStream srcdis ) throws IOException
   {
      int titlelen;
      byte titlebytes[];
      int itemLen;
      MenuItemRecord mir;

      MenuID     = srcdis.readShort();
      MenuWidth  = srcdis.readUnsignedShort();
      MenuHeight = srcdis.readUnsignedShort();
      MDEFResID  = srcdis.readShort();
      srcdis.skipBytes(2); // PlaceHolder
      InitEnabledState = srcdis.readInt(); // Will this capture the bits properly

      // Read Menu title
      titlelen = srcdis.readUnsignedByte();
      titlebytes = new byte[titlelen];
      srcdis.readFully(titlebytes);
      title = new String(titlebytes);

      // Ok start reading the menu items
      itemLen = srcdis.readUnsignedByte();
      while(itemLen != 0)
      {
         mir = new MenuItemRecord(itemLen);
         mir.read(srcdis);
         MenuItems.addElement(mir);
         itemLen = srcdis.readUnsignedByte();
      }
   }
}


/*=======================================================================*/
class MenuItemRecord
{
   /*--- Data -----------------------------------------------------------*/
   int textlen;
   String itemText;

   short icon_or_script;    // 1 byte
   byte key_equiv;        // 1 byte
   short mark_or_submenu; // 1 byte
   int style;             // 1 byte

   boolean isSeparator;
   boolean hasSubMenu;
   boolean reduceIconSize;
   boolean useSICN;
   boolean otherScript;
   boolean hasKeyEquiv;

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: MENURecord.java,v 1.2 2000/11/27 20:12:13 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   MenuItemRecord()
   {
      textlen = 0;
      init();
   }

   /*--------------------------------------------------------------------*/
   MenuItemRecord(int inTextLen)
   {
      textlen = inTextLen;
      init();
   }

   /*--------------------------------------------------------------------*/
   void init()
   {
      isSeparator    = false;
      hasSubMenu     = false;
      reduceIconSize = false;
      useSICN        = false;
      otherScript    = false;
      hasKeyEquiv    = false;
   }

   /*--------------------------------------------------------------------*/
   boolean hasIcon()
   {
      if(icon_or_script >=1 && icon_or_script <= 255 )
         return true;
      else
         return false;
   }

   /*--------------------------------------------------------------------*/
   short getIcon()
   {
      return (short)(icon_or_script + 256);
   }

   /*--------------------------------------------------------------------*/
   void read( DataInputStream srcdis ) throws IOException
   {
      byte textbytes[];

      if(textlen == 0) // 0 is a flag that len hasn't been read yet
         textlen = srcdis.readUnsignedByte();

      // Read Menu Item text
      textbytes = new byte[textlen];
      srcdis.readFully(textbytes);
      itemText = new String(textbytes);

      if(textlen == 1 && itemText.compareTo("-") == 0)
         isSeparator = true; // "-" char is sign that this is a separator

      icon_or_script  = (short)srcdis.readUnsignedByte();
      key_equiv       = srcdis.readByte();
      mark_or_submenu = (short)srcdis.readUnsignedByte();
      style           = srcdis.readUnsignedByte();

      switch(key_equiv)
      {
         case 0:
            break;
         case 0x1B:
            hasSubMenu = true;
            break;
         case 0x1C:
            otherScript = true;
            break;
         case 0x1D:
            reduceIconSize = true;
            break;
         case 0x1E:
            useSICN = true;
            break;
         default:
            if(key_equiv >= 0x01 && key_equiv <= 0x1A) hasKeyEquiv = false;
            else if(key_equiv >= 0x1F && key_equiv <= 0x20) hasKeyEquiv = false;
            else hasKeyEquiv = true;
            break;
      }
   }
}
