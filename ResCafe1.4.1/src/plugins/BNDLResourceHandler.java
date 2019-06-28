/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/BNDLResourceHandler.java,v 1.2 1999/10/21 21:13:07 gbsmith Exp $ */

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
 * $Log: BNDLResourceHandler.java,v $
 * Revision 1.2  1999/10/21 21:13:07  gbsmith
 * Added Copyright notice. Moved some JTable calls around
 *
 * Revision 1.1  1999/10/04 21:46:46  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class BNDLResourceHandler extends MacResourceHandler
{
   /*--- Data -----------------------------------------------------------*/
   JList resList;
   JTable resTable;
   private static final String[] columnNames = {
      "ResID", "Name", "Size", "AppSig", "SigRes", "ICN_Count", "FREF_Count" };
   //      0       1       2         3         4            5             6

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: BNDLResourceHandler.java,v 1.2 1999/10/21 21:13:07 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public String[] getTypes()
   {
      return new String[]{"BNDL"};
   }

   /*--------------------------------------------------------------------*/
   public void init( )
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
      String applicationSig = null;
      int sigResID          = 0;

      // ICN# Local ID mapping ( ResType should be 'ICN#' )
      int ICN_fam_count     = 0;
      // int first_icon_localID = 0;
      // int first_icon_resID   = 0;

      // FREF Local ID mapping ( ResType should be 'FREF' )
      int FREF_res_count = 0;

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
         try
         {
            byte tmpbytes[] = new byte[4];
            dis.readFully(tmpbytes, 0, 4);
            applicationSig = new String(tmpbytes);

            sigResID = dis.readShort();

            dis.skip(2); // Array count should always be 2

            dis.skip(4); // ResType should be 'ICN#'
            ICN_fam_count = dis.readShort() + 1;
            dis.skip(ICN_fam_count * 4); // Skip ICN ids for now...
            // ICN#s are: LocalID (2 bytes) and ResID (2 bytes)

            dis.skip(4); // ResType should be 'FREF'
            FREF_res_count = dis.readShort() + 1;
            dis.skip(FREF_res_count * 4); // Skip ICN ids for now...
            // FREFs are: LocalID (2 bytes) and ResID (2 bytes)
         } catch(IOException whatever) {
            System.err.println("ERROR: " + whatever);
         }

         // Set fields
         // "ResID", "Name", "Size"
         //      0       1       2
         tmpModel.setValueAt(new Short(currentRes.getID()),  i, 0);
         tmpModel.setValueAt(currentRes.getName(),           i, 1);
         tmpModel.setValueAt(new Integer(currentRes.size()), i, 2);

         //  "AppSig", "SigRes", "ICN_Count", "FREF_Count" };
         //        3         4            5             6
         tmpModel.setValueAt(applicationSig,                 i, 3);
         tmpModel.setValueAt(new Integer(sigResID),          i, 4);
         tmpModel.setValueAt(new Integer(ICN_fam_count),     i, 5);
         tmpModel.setValueAt(new Integer(FREF_res_count),    i, 6);

         i++;
      }

      resTable = new JTable(tmpModel);
      JScrollPane rtsp = new JScrollPane(resTable);
      add(rtsp, "Center");
   }

   /*--------------------------------------------------------------------*/
   public void save ( String inType, File savedir )
   {
      StringBuffer tmpfilename;
      String filename;
      String saveType;

      if(resData == null)
      {
         System.err.println("ERROR: No resources of type \'" + inType +
                            "\' to save");
         return;
      }

      Resource myResArray[] = resData.getResArray();

      System.out.println("Saving resources of type \'" + inType + "\' as bytes");
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
