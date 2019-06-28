/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/jMainResourceView.java,v 1.16 2000/12/11 05:58:40 gbsmith Exp $ */

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.KeyStroke;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import ResourceManager.*;

/*=======================================================================*/
/* Copyright (c) 1999-2000 by G. Brannon Smith -- All Rights Reserved    */
/*=======================================================================*/

/*=======================================================================*/
/*
 * $Log: jMainResourceView.java,v $
 * Revision 1.16  2000/12/11 05:58:40  gbsmith
 * Added KeyListener module to implement document switching via
 * 'Ctrl-' and 'Ctrl+' keystrokes
 *
 * Revision 1.15  2000/11/27 19:50:05  gbsmith
 * Split constructor into short constructor and assemble() method. Eliminated
 * some commented code. Used Unicode \u00e9 for e-aigu. Incremented version
 * to 1.3.
 *
 * Revision 1.14  2000/05/25 06:48:56  gbsmith
 * Removed references to Jimi. Moved menu instantiation code
 * from constructor to its own method - buildMenus().
 *
 * Revision 1.13  2000/05/24 06:54:46  gbsmith
 * Added the handlerView functionality to allow a GUI component to
 * display details about the loaded handler plugins rather than
 * stdout. Still in VERY EARLY stage.
 * Updated version to 1.2
 *
 * Revision 1.12  1999/10/28 20:30:28  gbsmith
 * Changed version number to 1.1
 *
 * Revision 1.11  1999/10/28 04:40:59  gbsmith
 * Fixed 'Close' accelerator and added check box for current doc
 * in menu.
 *
 * Revision 1.10  1999/10/28 03:56:46  gbsmith
 * Changed to support loading of multiple files:
 *    - Major data struct is now DocumentManager
 *    - Added 'Documents' menu for switching between them
 *    - Changed update method to support all this
 *
 * Revision 1.9  1999/10/27 07:13:19  gbsmith
 * Spun off icon management to a subclass to facilitate threaded
 * loading. Merged type display menu items into a single toggled item.
 * Added menu accelerators and mnemonics.
 *
 * Revision 1.8  1999/10/21 23:44:26  gbsmith
 * Added Copyright notice. Made class imports more explicit.
 * Changed setFileController code a little.
 *
 * Revision 1.7  1999/10/19 06:14:32  gbsmith
 * Added help menu with items for requesting info about the application
 * and about the currently displayed plugin handler.
 *
 * Revision 1.6  1999/10/18 00:08:12  gbsmith
 * Added menu items and calls for requesting listings of supported
 * types and registered handlers.
 *
 * Revision 1.5  1999/10/16 02:19:17  gbsmith
 * Added menu and items to control handlers - one to Rescan or reload them (so
 * they can be changed while running) and one to list the handlers and type
 * (not yet implemented)
 *
 * Revision 1.4  1999/10/13 23:42:47  gbsmith
 * Updated to conform with new MacResourceHandler calls. Removed useless
 * 'Save' menu item.
 *
 * Revision 1.3  1999/10/04 21:42:57  gbsmith
 * Made class imports more explicit. Added save and show items to menu.
 * Converted to SplitPane layout. A few other adjustments...
 *
 * Revision 1.2  1999/10/02 03:55:36  gbsmith
 * Moved main method and MVC management out to a top level app class,
 * ResCafe.
 *
 * Revision 1.1  1999/09/30 05:12:56  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
public class jMainResourceView extends JFrame implements Observer
{
   /*--- Data -----------------------------------------------------------*/
   /*------ GUI ---------------------------------------------------------*/
   // Menu stuff
   JMenuBar mbar;
   JMenu fileMenu, typeMenu, handlerMenu, docMenu, helpMenu;
   JMenuItem openItem, saveAllItem, saveHandledItem, saveCurrentItem,
      closeItem, closeAllItem, quitItem;
   JMenuItem typeItem;
   JMenuItem rescanItem, listTypeItem, listHandlerItem;
   JMenuItem aboutAppItem, aboutPlugItem;

   // Panel stuff
   JPanel typePanel, handlerPanel, typeLabPanel, handlerLabPanel, filePanel;
   JLabel typeLab, handlerLab, fnlab;

   JSplitPane  jsp;
   JScrollPane lsp;
   JList myTypeList;

   MacResourceHandler currentHandler;
   String currentType;

   jHandlerView hview = null;

   /*------ Models --------------------------------------------------------*/
   ResourceModel   currentResMod;
   DocumentManager docmgr;

   HandlerTable    handlers; // Not observables but we'll call
   IconTable       icons;    // them models anyway

   /*------ Controllers ---------------------------------------------------*/
   FileController     flistener;
   WindowController   locWinListener;
   DocController      locDocListener;
   MenuItemController locMenuListener;
   TypeListController locListListener;

   /*------ Misc ----------------------------------------------------------*/
   Thread iconThread, handlerThread; // for loading the icons, handlers
   int currentDocIndex;

   /*------ RCS -----------------------------------------------------------*/
   static final String rcsid =
   "$Id: jMainResourceView.java,v 1.16 2000/12/11 05:58:40 gbsmith Exp $";

   /*--- Methods ----------------------------------------------------------*/
   public jMainResourceView(String frameTitle) { super(frameTitle); }

   /*----------------------------------------------------------------------*/
   public void assemble()
   {
      Container contentPane = getContentPane();

      /* Load type icons with a thread ------------------------------------*/
      icons = new IconTable("icons");
      iconThread = new Thread(icons);
      iconThread.start();

      /* Set up menubar and menus -----------------------------------------*/
      buildMenus();

      /* Setup Panels -----------------------------------------------------*/
      typePanel    = new JPanel();
      handlerPanel = new JPanel();
      contentPane.add(jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                           false, typePanel, handlerPanel ),
                      "Center");
      jsp.setOneTouchExpandable(true);

      /* Set up list of Resource Types found ------------------------------*/
      typePanel.setLayout(new BorderLayout());

      typeLabPanel = new JPanel();
      typeLabPanel.setBorder(BorderFactory.createRaisedBevelBorder());
      typePanel.add(typeLabPanel, "North");

      typeLab = new JLabel("Resource Types");
      typeLabPanel.add(typeLab);

      myTypeList = new JList();
      lsp = new JScrollPane(myTypeList);
      typePanel.add(lsp, "Center");

      /* Set up area for Resource Handler display -------------------------*/
      handlerPanel.setLayout(new BorderLayout());

      handlerLabPanel = new JPanel();
      handlerLabPanel.setBorder(BorderFactory.createRaisedBevelBorder());
      handlerPanel.add(handlerLabPanel, "North");

      handlerLab = new JLabel("Resources");
      handlerLabPanel.add(handlerLab);

      /* Setup file name display ------------------------------------------*/
      filePanel = new JPanel();
      filePanel.setBorder(BorderFactory.createEtchedBorder(
         getBackground().brighter(), getBackground().darker()));
      fnlab = new JLabel("*no file loaded*");
      filePanel.add(fnlab);
      contentPane.add(filePanel, "South");

      /* Setup event listeners local to this view -------------------------*/
      addWindowListener(locWinListener = new WindowController());

      myTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      myTypeList.addListSelectionListener(locListListener =
                                          new TypeListController());

      addKeyListener(locDocListener);

      /* Reset split pane to nicer proportions ----------------------------*/
      jsp.setDividerLocation(jsp.getMinimumDividerLocation());

      currentHandler = null;
      currentType    = null;
   }

   /*----------------------------------------------------------------------*/
   private void buildMenus()
   {
      mbar = new JMenuBar();

      /*-----------*/
      /*    File   */
      /*-------------------------------------------------------------------*/
      fileMenu = new JMenu("File", true);
      fileMenu.setMnemonic('F');
      /*-------------------------------------------------------------------*/
      fileMenu.add(openItem        = new JMenuItem("Open...", 'O'));
      openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                     Event.CTRL_MASK));

      fileMenu.add(saveCurrentItem = new JMenuItem("Save Current...", 'S'));
      saveCurrentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                     Event.CTRL_MASK));

      fileMenu.add(saveHandledItem = new JMenuItem("Save Handled...", 'H'));
      saveHandledItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                                                     Event.CTRL_MASK));

      fileMenu.add(saveAllItem     = new JMenuItem("Save All...", 'A'));
      saveAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                                                     Event.CTRL_MASK));

      fileMenu.add(closeItem = new JMenuItem("Close", 'C'));
      closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                      Event.CTRL_MASK));

      fileMenu.add(closeAllItem = new JMenuItem("Close All"));

      fileMenu.addSeparator(); /*------------------------------------------*/

      fileMenu.add(quitItem        = new JMenuItem("Quit", 'Q'));
      quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                                                     Event.CTRL_MASK));
      /*-------------------------------------------------------------------*/


      /*-----------*/
      /*   Type    */
      /*-------------------------------------------------------------------*/
      typeMenu = new JMenu("Type", true);
      typeMenu.setMnemonic('T');
      /*-------------------------------------------------------------------*/
      typeMenu.add(typeItem = new JMenuItem("Show Handled Types", 'T'));
      typeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                                                     Event.CTRL_MASK));
      /*-------------------------------------------------------------------*/

      /*-----------*/
      /* Handlers  */
      /*-------------------------------------------------------------------*/
      handlerMenu = new JMenu("Handlers", true);
      handlerMenu.setMnemonic('a');
      /*-------------------------------------------------------------------*/
      handlerMenu.add(rescanItem     = new JMenuItem("Rescan Handlers", 'R'));
      rescanItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                                     Event.CTRL_MASK));
      handlerMenu.add(listHandlerItem = new JMenuItem("List Handlers by Type", 'H'));
      handlerMenu.add(listTypeItem    = new JMenuItem("List Types by Handler", 'T'));
      // These names and descriptions can be confusing
      /*-------------------------------------------------------------------*/


      /*-----------*/
      /* Documents */
      /*-------------------------------------------------------------------*/
      docMenu = new JMenu("Documents", true);
      docMenu.setMnemonic('d');
      /*-------------------------------------------------------------------*/
      /* Will dynamically                                                  */
      /* list open docs here                                               */
      /*                                                                   */
      /*-------------------------------------------------------------------*/

      /*-----------*/
      /*   Help    */
      /*-------------------------------------------------------------------*/
      helpMenu = new JMenu("Help", true);
      helpMenu.setMnemonic('H');
      /*-------------------------------------------------------------------*/
      helpMenu.add(aboutAppItem  = new JMenuItem("About ResCaf\u00e9", 'R'));
      aboutAppItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
                                                         Event.CTRL_MASK));

      helpMenu.add(aboutPlugItem = new JMenuItem("About Plugin", 'P'));
      aboutPlugItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                                                         Event.CTRL_MASK));
      /*-------------------------------------------------------------------*/

      mbar.add(fileMenu);
      mbar.add(typeMenu);
      mbar.add(handlerMenu);
      mbar.add(docMenu);
      mbar.add(helpMenu); // Wish this could go at extreme right of MenuBar...

      setJMenuBar(mbar);

      /* Setup menu event listeners local to this view --------------------*/
      locDocListener  = new DocController();
      locMenuListener = new MenuItemController();

      quitItem.addActionListener(locMenuListener);

      typeItem.addActionListener(locMenuListener);

      rescanItem.addActionListener(locMenuListener);
      listHandlerItem.addActionListener(locMenuListener);
      listTypeItem.addActionListener(locMenuListener);

      aboutAppItem.addActionListener(locMenuListener);
      aboutPlugItem.addActionListener(locMenuListener);
   }

   /*----------------------------------------------------------------------*/
   public void setDocManager(DocumentManager inmgr)
   {
      docmgr = inmgr;
      docmgr.addObserver(this);
      rebuildDocMenu();
   }

   /*--------------------------------------------------------------------*/
   public void setHandlers(HandlerTable intab) {  handlers = intab;   }

   /*--------------------------------------------------------------------*/
   public void setFileController(FileController infc)
   {
      // NOTE: this is not "addFileController" because it is assumed that
      //       there is just one of them
      if(flistener == infc) return;

      //This code could cause problems on Solaris
      if(flistener != null)
      {
         openItem.removeActionListener(flistener);
         saveAllItem.removeActionListener(flistener);
         saveHandledItem.removeActionListener(flistener);
         saveCurrentItem.removeActionListener(flistener);
         closeItem.removeActionListener(flistener);
         closeAllItem.removeActionListener(flistener);
      }

      flistener = infc;

      openItem.addActionListener(flistener);
      saveAllItem.addActionListener(flistener);
      saveHandledItem.addActionListener(flistener);
      saveCurrentItem.addActionListener(flistener);
      closeItem.addActionListener(flistener);
      closeAllItem.addActionListener(flistener);

      flistener.setCurrentType( null );
   }

   /*--------------------------------------------------------------------*/
   public String getCurrentType()
   {
      return currentType;
   }

   /*--------------------------------------------------------------------*/
   public void update(Observable o, Object arg)
   {
      ResourceModel tmpResMod = docmgr.getCurrent();
      if(tmpResMod == currentResMod) return; // No real change

      currentResMod = tmpResMod;

      resetView();

      // Be sure all the icons are loaded
      try
      {
         iconThread.join();
      } catch (InterruptedException intex) {
         System.err.println("ERROR loading icons: " + intex);
      }

      rebuildDocMenu();

      if(currentResMod != null) showAllTypes();
   }

   /*--------------------------------------------------------------------*/
   void resetView()
   {
      if(currentResMod == null) fnlab.setText("*no file loaded*");
      else                      fnlab.setText(currentResMod.getFilename());

      // Clean out old display
      if(currentHandler != null) handlerPanel.remove(currentHandler);
      currentHandler = null;
      if(flistener != null) flistener.setCurrentType(null);

      // Excessive way to clear
      myTypeList.setModel(new MacResTypeListModel(icons));

      repaint();
   }


   /*--------------------------------------------------------------------*/
   void rebuildDocMenu()
   {
      String[] docs = docmgr.listDocuments();
      String currentDoc = docmgr.getCurrentName();
      JMenuItem docItem;
      docMenu.removeAll();
      if(locDocListener == null) locDocListener = new DocController();

      for(int i = 0; i < docs.length; i++)
      {
         if(docs[i].compareTo(currentDoc) == 0)
         {
            currentDocIndex = i;
            docItem = new JCheckBoxMenuItem(docs[i], true);
         } else docItem = new JMenuItem(docs[i]);

         docItem.addActionListener(locDocListener);
         docMenu.add(docItem);
      }
   }

   /*--------------------------------------------------------------------*/
   void showAllTypes()
   {
      MacResTypeListModel tmpModel = new MacResTypeListModel(icons);
      ListCellRenderer renderer = new MacResTypeListCellRenderer();
      String s;

      Enumeration typeKeys = currentResMod.getTypes();
      if(typeKeys == null) return;
      while( typeKeys.hasMoreElements() )
      {
         s = (String)typeKeys.nextElement();
         tmpModel.addInOrder(s);
      }

      myTypeList.setModel(tmpModel);
      myTypeList.setCellRenderer(renderer);

      // Ready menu for next selection
      typeItem.setText("Show Handled Types");
      typeItem.setMnemonic('H');

      repaint();
   }

   /*--------------------------------------------------------------------*/
   void showHandledTypes()
   {
      MacResTypeListModel tmpModel = new MacResTypeListModel(icons);
      ListCellRenderer renderer = new MacResTypeListCellRenderer();
      String s;

      Enumeration typeKeys = currentResMod.getTypes();
      if(typeKeys == null) return;
      while( typeKeys.hasMoreElements() )
      {
         s = (String)typeKeys.nextElement();
         if(handlers.canHandleType(s)) tmpModel.addInOrder(s);
      }

      myTypeList.setModel(tmpModel);
      myTypeList.setCellRenderer(renderer);

      // Ready menu for next selection
      typeItem.setText("Show All Types");
      typeItem.setMnemonic('A');

      repaint();
   }

   /*--------------------------------------------------------------------*/
   private void updateResType()
   {
      // Figure out what type was selected...
      // This seems kinda awkward - is there a simpler way?
      if(!myTypeList.isSelectionEmpty())
      {
         Object whichTypeObj[] = (Object[])myTypeList.getModel().
            getElementAt(myTypeList.getMaxSelectionIndex());

         String whichType = (String)whichTypeObj[0];
         handleType(whichType);
      }
   }

   /*--------------------------------------------------------------------*/
   private void handleType(String whichType)
   {
      currentType = whichType;
      if(flistener != null) flistener.setCurrentType(whichType);

      if(currentHandler != null) handlerPanel.remove( currentHandler );

      /* This will eventually check the type of handler to use */
      if(handlers.canHandleType(currentType))
      {
         try
         {
            currentHandler =
               (MacResourceHandler)handlers.
               getHandler(currentType).newInstance();
         } catch (Exception e) { System.err.println(e); }

         handlerLab.setText("" + currentResMod.getCountOfType(currentType) +
                            " resources of type \'" + currentType +
                            "\' handled by " +
                            currentHandler.getClass().getName());
      } else {
         currentHandler = new DefaultResourceHandler();
         handlerLab.setText("" + currentResMod.getCountOfType(currentType) +
                            " resources of type \'" + currentType +
                            "\' handled by Default Handler");
      }

      currentHandler.setResData(currentResMod.getResourceType(currentType));
      currentHandler.setResModel(currentResMod);
      currentHandler.init();
      currentHandler.display();
      handlerPanel.add(currentHandler, "Center");

      Dimension hpdim = handlerPanel.getSize();
      Dimension hldim = handlerLab.getSize();

      handlerPanel.invalidate ();
      handlerPanel.validate ();
      handlerPanel.repaint ();
   }

   /*--------------------------------------------------------------------*/
   private void aboutResCafe()
   {
      JOptionPane jop = new JOptionPane();
      String message[] = {
         "ResCaf\u00e9 v1.4",
         "by G. Brannon Smith <gbsmith@mail.com>",
         "Sun, 10 Dec 2000",
         " ",
         "A Java app for viewing and extracting data",
         "from Mac Resource Forks on other platforms -",
         "principally Linux"
      };

      jop.showMessageDialog(
         this,     // Parent
         message,  // "about message here",
         "About ResCaf\u00e9",
         JOptionPane.INFORMATION_MESSAGE
         );
   }

   /*--------------------------------------------------------------------*/
   private void aboutCurrentPlugin()
   {
      int t,m;

      JOptionPane jop = new JOptionPane();

      String about[], types[], message[];
      StringBuffer typelist;

      if(currentHandler == null) return;
      else
      {
         types = currentHandler.getTypes();
         about = currentHandler.about();
         if(about == null) about = new String[]{"No information available",
                                                "for current plugin"};
      }

      typelist = new StringBuffer("Handled Types: ");
      for(t = 0; t < types.length ; t++)
      {
         typelist.append("\'" + types[t] + "\'");
         if( t < types.length - 1 ) typelist.append(", ");
      }

      message = new String[about.length + 2];
      for(m = 0; m < about.length; m++) message[m] = about[m];
      message[m++] = " ";
      message[m++] = typelist.toString();

      jop.showMessageDialog(
         this,     // Parent
         message,  // "about message here",
         "About Current Plugin",
         JOptionPane.INFORMATION_MESSAGE
         );
   }

   /*--------------------------------------------------------------------*/
   private void doQuit()
   {
      boolean success = false;

      dispose();
      System.exit(0);
   }


   /*====================================================================*/
   // Below are inner Controller/Listener classes to manage events local
   // to this view. The outside Models & Views are unaffected.
   /*====================================================================*/
   class MenuItemController implements ActionListener
   {
      // Local because it only affects aspects of the local display:
      // *** HOWEVER, perhaps quit portion should not be local
      /*------ RCS ---------------------------------------------------------*/
      final String rcsid = "$Id: jMainResourceView.java,v 1.16 2000/12/11 05:58:40 gbsmith Exp $";

      /*--------------------------------------------------------------------*/
      public void actionPerformed(ActionEvent event)
      {
         JMenuItem item = (JMenuItem)event.getSource();
         if     (item == quitItem)        doQuit();
         else if(item == typeItem)
         {
            if(typeItem.getText().compareTo("Show All Types") == 0)
               showAllTypes();
            else showHandledTypes();
         }

         else if(item == rescanItem) // Should probably move this out to
         {                           // an outside controller
            handlerThread = new Thread(handlers);
            handlerThread.start();
         }

         // Currently only console versions - not windows
         else if(item == listHandlerItem)
         {
            if(hview == null) hview = jHandlerView.getInstance();
            hview.show();
         }

         else if(item == listTypeItem)
         {
            if(hview == null) hview = jHandlerView.getInstance();
            hview.show();
         }

         else if(item == aboutAppItem)    aboutResCafe();
         else if(item == aboutPlugItem)   aboutCurrentPlugin();
         else System.err.println("Invalid Menu Item");
      }
   }

   /*====================================================================*/
   class DocController extends KeyAdapter implements ActionListener
   {
      // Local because it only affects what ResourceModel is VIEWED
      /*------ RCS ---------------------------------------------------------*/
      final String rcsid = "$Id: jMainResourceView.java,v 1.16 2000/12/11 05:58:40 gbsmith Exp $";

      /*--------------------------------------------------------------------*/
      public void actionPerformed(ActionEvent ae)
      {
         String command = ae.getActionCommand();
         docmgr.choose(command);
      }

      /*--------------------------------------------------------------------*/
      public void keyTyped(KeyEvent ae)
      {
         char whichKey;

         if(ae.isControlDown())
         {
            whichKey = ae.getKeyChar();
            if(whichKey == '+') { ae.consume(); selectNextDoc(); }
            if(whichKey == '-') { ae.consume(); selectPrevDoc(); }
         }
      }

      /*--------------------------------------------------------------------*/
      void selectNextDoc()
      {
         int numdocs, nextdoc;

         numdocs = docMenu.getItemCount();
         if(numdocs < 2) return;

         nextdoc = (currentDocIndex + 1)%numdocs;
         docMenu.getItem(nextdoc).doClick();
      }

      /*--------------------------------------------------------------------*/
      void selectPrevDoc()
      {
         int numdocs, prevdoc;

         numdocs = docMenu.getItemCount();
         if(numdocs < 2) return;

         prevdoc = (currentDocIndex - 1 + numdocs)%numdocs;
         docMenu.getItem(prevdoc).doClick();
      }
   }

   /*====================================================================*/
   class TypeListController implements ListSelectionListener
   {
      // Local because it only affects position of scroller
      // Detects clicks in the Type List and displays resources of that type

      /*------ RCS ---------------------------------------------------------*/
      final String rcsid = "$Id: jMainResourceView.java,v 1.16 2000/12/11 05:58:40 gbsmith Exp $";

      /*--------------------------------------------------------------------*/
      public void valueChanged(ListSelectionEvent lse)
      {
         if(!lse.getValueIsAdjusting())
         {
            int id_index = lse.getLastIndex();
            updateResType();
         }
      }
   }


   /*====================================================================*/
   class WindowController extends WindowAdapter
   {
      // *** Perhaps this shouldn't be local after all
      /*------ RCS ---------------------------------------------------------*/
      final String rcsid = "$Id: jMainResourceView.java,v 1.16 2000/12/11 05:58:40 gbsmith Exp $";
      public void windowClosing(WindowEvent event) { doQuit(); }
   }
}
