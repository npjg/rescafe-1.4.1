/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/IconTable.java,v 1.3 2000/12/12 07:44:21 gbsmith Exp $ */

import javax.swing.ImageIcon;

import java.util.Hashtable;

import java.io.File;
import java.io.FileReader;

import java.net.URL;

/*=======================================================================*/
/*
 * $Log: IconTable.java,v $
 * Revision 1.3  2000/12/12 07:44:21  gbsmith
 * Now finds location of own class file and uses it to find
 * the "icons" dir
 *
 * Revision 1.2  2000/05/25 05:55:31  gbsmith
 * Removed Jimi calls and replaced with Custom XpmImage class
 *
 * Revision 1.1  1999/10/27 07:13:41  gbsmith
 * Initial revision
 *
 *
 */

/*=======================================================================*/
class IconTable extends Hashtable implements Runnable
{
   /*--- Data -----------------------------------------------------------*/
   String icon_dirname;

   /*----- RCS ----------------------------------------------------------*/
   static final String rcsid = "$Id: IconTable.java,v 1.3 2000/12/12 07:44:21 gbsmith Exp $";

   /*--- Methods --------------------------------------------------------*/
   IconTable(String indir)
   {
      super();
      icon_dirname = indir;
   }

   /*--------------------------------------------------------------------*/
   public void run() // For threaded loading
   {
      load();
   }

   /*--------------------------------------------------------------------*/
   void load()
   {
      String icon_filenames[];
      String icontype;
      String myPath;
      Class me;
      URL myURL;
      File   icondir;
      XpmImage myxpm;

      // The "icons" dir is in the same dir as the jar file
      //

      // Find myself (Who am I?! WHERE did I come from?!)
      me = this.getClass();
      myURL = me.getResource(me.getName() + ".class");
      
      // Convert to path - chop off protocol
      myPath = myURL.getFile();
      myPath = myPath.substring(myPath.indexOf(':') + 1);

      // Goto up two levels (self + JAR file)
      icondir = new File(myPath).getParentFile().getParentFile();
      icondir = new File(icondir, icon_dirname);
      
      // Get file list - assume everything in dir is an image
      icon_filenames = icondir.list();

      // Load them and put into hash
      for(int i = 0; i < icon_filenames.length; i++)
      {
         icontype =
            icon_filenames[i].substring(0, icon_filenames[i].lastIndexOf('.'));
         icontype = icontype.replace('_', ' '); // Interpret underscores as spaces

         if(icon_filenames[i].endsWith(".xpm") || 
            icon_filenames[i].endsWith(".XPM"))
            try
            {
               myxpm = new XpmImage(
                  new FileReader(new File(icondir, icon_filenames[i])));
               put(icontype, new ImageIcon(myxpm.getImage()));
            } catch(Exception whatever) { }
      }
   }
}
