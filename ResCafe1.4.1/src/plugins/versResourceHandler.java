/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/versResourceHandler.java,v 1.2 1999/10/21 22:23:50 gbsmith Exp $ */

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Image;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Enumeration;

import ResourceManager.*;


/*=======================================================================*/
/*
 * $Log: versResourceHandler.java,v $
 * Revision 1.2  1999/10/21 22:23:50  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 *
 * Revision 1.1  1999/10/04 22:38:26  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class versResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   JList resList;
   JTable resTable;
   private static final String[] columnNames = {
      "ResID", "Name", "Size", "Major", "Minor", "Dev", "Pre", "Region", "Vers#", "Msg"};
     //    0       1       2        3        4      5      6         7        8      9
   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: versResourceHandler.java,v 1.2 1999/10/21 22:23:50 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"vers"};
   }

   /*--------------------------------------------------------------------*/
   public void init()
   {
   }

   /*--------------------------------------------------------------------*/
   public void display()
   {
      byte rawData[];
      DefaultTableModel tmpModel;
      Enumeration resIDs;
      Resource currentRes;
      DataInputStream dis;

      // Fields
      int major_rev_level = 0;
      int minor_rev_level = 0;
      int dev_stage       = 0;
      String dev_str      = null;

      int prerelease_rev_level = 0;
      int region_code    = 0;
      byte tmpbytes[]    = null;
      int tmplen         = 0;
      String version_str = null;
      String version_msg = null;

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

         rawData = currentRes.getData();
         dis = new DataInputStream(new ByteArrayInputStream(rawData));

         // Fields
         try
         {
            major_rev_level = dis.readUnsignedByte();
            minor_rev_level = dis.readUnsignedByte();
            dev_stage       = dis.readUnsignedByte();

            switch(dev_stage)
            {
               case 0x20: dev_str = "Prealpha"; break;
               case 0x40: dev_str = "Alpha";    break;
               case 0x60: dev_str = "Beta";     break;
               case 0x80: dev_str = "Final";    break;
               default:   dev_str = null;
            }

            prerelease_rev_level = dis.readUnsignedByte();
            region_code     = dis.readUnsignedShort();

            tmplen = dis.readUnsignedByte();
            tmpbytes = new byte[tmplen];
            dis.readFully(tmpbytes, 0, tmplen);
            version_str = new String(tmpbytes);

            tmplen = dis.readUnsignedByte();
            tmpbytes = new byte[tmplen];
            dis.readFully(tmpbytes, 0, tmplen);
            version_msg = new String(tmpbytes);
         } catch(IOException whatever) {
            System.err.println("ERROR: " + whatever);
         }

         // Set fields
         // "Major", "Minor", "Dev", "Pre", "Region", "Vers#", "Msg"};
         //      3        4      5      6         7        8      9
         tmpModel.setValueAt(new Integer(major_rev_level),      i, 3);
         tmpModel.setValueAt(new String(""  + ((minor_rev_level >> 4) & 0x0F) +
                                        "." + (minor_rev_level & 0x0F)),
                             i, 4);

         //tmpModel.setValueAt(new Integer(minor_rev_level),      i, 4);
         tmpModel.setValueAt(dev_str,                           i, 5);
         tmpModel.setValueAt(new Integer(prerelease_rev_level), i, 6);
         tmpModel.setValueAt(new Integer(prerelease_rev_level), i, 6);
         tmpModel.setValueAt(new Integer(region_code),          i, 7);
         tmpModel.setValueAt(version_str,                       i, 8);
         tmpModel.setValueAt(version_msg,                       i, 9);

         i++;
      }

      resTable = new JTable(tmpModel);
      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   public void save ( File savedir )
   {
      StringBuffer tmpfilename;
      String filename;
      String saveType;

      if(resData == null)
      {
         System.err.println("ERROR: No resources to save");
         return;
      }

      saveType = resData.getID();

      Resource myResArray[] = resData.getResArray();

      System.out.println("Saving resources of type \'" + saveType + "\' as bytes");
      for(int i=0; i < myResArray.length; i++)
      {
         tmpfilename = new StringBuffer(savedir.getPath());
         tmpfilename.append( File.separator + myResArray[i].getID() );
         if(myResArray[i].getName() != null)
            tmpfilename.append("_" + myResArray[i].getName());
         tmpfilename.append(".raw");
         filename = tmpfilename.toString().replace(' ', '_');
         //System.out.println("\tSaving \'" + filename + "\'...");

         try
         {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(myResArray[i].getData());
         } catch (Exception whatever) {
            System.err.println("ERROR: Got exception " + whatever );
         }
      }
   }
}
