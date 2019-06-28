/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/plugins/RCS/Palette.java,v 1.2 1999/10/21 21:46:11 gbsmith Exp $ */

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;


/*=======================================================================*/
/*
 * $Log: Palette.java,v $
 * Revision 1.2  1999/10/21 21:46:11  gbsmith
 * Added Copyright notice.
 *
 * Revision 1.1  1999/10/04 22:05:03  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
class Palette
{
   /*--- Data -----------------------------------------------------------*/
   int numColors;
   int reds[];

   int greens[];
   int blues[];

   /*------ RCS ---------------------------------------------------------*/
   static final String rcsid = "$Id: Palette.java,v 1.2 1999/10/21 21:46:11 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   public void parse(byte[] indata)
   {
      DataInputStream dis;
      dis = new DataInputStream(new ByteArrayInputStream(indata));

      try
      {
         numColors = dis.readUnsignedShort();
         reds   = new int[numColors];
         greens = new int[numColors];
         blues  = new int[numColors];

         dis.skip(14); // Dummy data?

         for(int j = 0; j < numColors; j++)
         {
            reds[j]   = dis.readUnsignedShort();
            greens[j] = dis.readUnsignedShort();
            blues[j]  = dis.readUnsignedShort();
            dis.skip(10); // Dummy data?
         }
      } catch (IOException whatever) {
         System.err.println("ERROR: " + whatever);
      }
   }

   public void printJava(PrintWriter ps)
   {
      ps.println("int numColors = " + numColors + ";");
      ps.println("");

      ps.print("int reds[] = { ");
      for(int r = 0; r < reds.length; r++)
         ps.print("" + reds[r] + ", ");
      ps.println(" } ;");
      ps.println("");

      ps.print("int greens[] = { ");
      for(int g = 0; g < greens.length; g++)
         ps.print("" + greens[g] + ", ");
      ps.println(" } ;");
      ps.println("");

      ps.print("int blues[] = { ");
      for(int b = 0; b < blues.length; b++)
         ps.print("" + blues[b] + ", ");
      ps.println(" } ;");
      ps.println("");
   }
}
