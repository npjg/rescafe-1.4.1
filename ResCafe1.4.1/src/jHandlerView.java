/* $Header: /home/gbsmith/projects/ResCafe/ResCafe1.4/src/RCS/jHandlerView.java,v 1.3 2000/11/27 19:54:38 gbsmith Exp $ */

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Container;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

/*=======================================================================*/
/*
 * $Log: jHandlerView.java,v $
 * Revision 1.3  2000/11/27 19:54:38  gbsmith
 * Removing "Refreshing..." notification since update is being called much
 * more often now.
 *
 * Revision 1.2  2000/05/24 06:58:51  gbsmith
 * Added RCS Id tag
 *
 * Revision 1.1  2000/05/24 06:31:41  gbsmith
 * Initial revision
 *
 */

/*=======================================================================*/
/* Copyright (c) 1999 by G. Brannon Smith -- All Rights Reserved         */
/*=======================================================================*/

/*=======================================================================*/
public class jHandlerView extends JFrame implements Observer // SINGLETON
{
   /* This is far from complete - perhaps it is even a bad approach.
      Still I would like to get it working halfway decently. */

   /*--- RCS id ---------------------------------------------------------*/
   static String rcsid="$Id: jHandlerView.java,v 1.3 2000/11/27 19:54:38 gbsmith Exp $";

   /*--- Data -----------------------------------------------------------*/
   private static jHandlerView _instance = null;

  /*------ GUI ---------------------------------------------------------*/
   JPanel mainPanel, buttonArea, consolePanel, listPanel;
   JTabbedPane mainJTPane;
   JButton closeButton, refreshButton, rescanButton;
   JTable listTable;
   String columnNames[] = {"Type", "Handler"};


   /*------ Models --------------------------------------------------------*/
   HandlerTable handlerModel;

   /*------ Controllers ---------------------------------------------------*/
   WindowController  locWinListener;
   ButtonController  locButtonListener;

   /*--- Methods ----------------------------------------------------------*/
   static jHandlerView getInstance()
   {
      // Be sure an instance exists
      if(_instance == null) _instance = new jHandlerView("Handlers");
      return _instance;
   }

   /*--------------------------------------------------------------------*/
   private jHandlerView(String frameTitle)
   {
      super(frameTitle);
      Container contentPane = getContentPane();

      contentPane.setLayout(new BorderLayout());

      mainJTPane = new JTabbedPane();
      contentPane.add(mainJTPane, "Center");
      consolePanel = new JPanel();
      listPanel = new JPanel();
      mainJTPane.add(listPanel, "List");
      mainJTPane.add(consolePanel, "Console");

      buttonArea = new JPanel();
      contentPane.add(buttonArea, "South");
      rescanButton  = new JButton("Rescan");
      refreshButton = new JButton("Refresh");
      closeButton   = new JButton("Close");

      buttonArea.add(refreshButton);
      buttonArea.add(rescanButton);
      buttonArea.add(closeButton);

      locButtonListener = new ButtonController();
      refreshButton.addActionListener(locButtonListener);
      closeButton.addActionListener(locButtonListener);

      addWindowListener(locWinListener = new WindowController());
   }

   /*--------------------------------------------------------------------*/
   public void setHandlerModel(HandlerTable inht)
   {
      if(handlerModel != null) handlerModel.deleteObserver(this);
      handlerModel = inht;
      handlerModel.addObserver(this);
   }

   /*--------------------------------------------------------------------*/
   public void update(Observable o, Object arg)
   {
      refresh();
   }

   /*--------------------------------------------------------------------*/
   public void setTheTable( )
   {
      System.out.println("Setting the Table...");
      DefaultTableModel tmpModel;
      Enumeration typeKeys;
      int typeCount;
      String currentType;

      if(handlerModel == null) return; // Can't display a null model

      typeCount = handlerModel.getTypeCount();
      tmpModel = new DefaultTableModel(columnNames, typeCount);

      int i = 0;
      typeKeys = handlerModel.getTypeKeys();
      while(typeKeys.hasMoreElements())
      {
         currentType = typeKeys.nextElement().toString();
         tmpModel.setValueAt(currentType,                              i, 0);
         tmpModel.setValueAt(handlerModel.getHandlerName(currentType), i, 1);
         i++;
      }


      listTable = new JTable(tmpModel);
      JScrollPane ltsp = new JScrollPane(listTable);
//            listPanel.add(ltsp, "Center");
      listPanel.add(ltsp);
      repaint();
   }

   /*--------------------------------------------------------------------*/
   public void refresh( )
   {
      DefaultTableModel tmpModel;
      Enumeration typeKeys;
      int typeCount;
      String currentType;

      if(listTable == null)
      {
         setTheTable();
         return;
      }

      //System.out.println("Refreshing...");
      if(handlerModel == null)
      {
         listTable.setModel(null);
         return; // Can't display a null model
      }

      typeCount = handlerModel.getTypeCount();
      tmpModel = new DefaultTableModel(columnNames, typeCount);

      int i = 0;
      typeKeys = handlerModel.getTypeKeys();
      while(typeKeys.hasMoreElements())
      {
         currentType = typeKeys.nextElement().toString();
         tmpModel.setValueAt(currentType,                              i, 0);
         tmpModel.setValueAt(handlerModel.getHandlerName(currentType), i, 1);
         i++;
      }

      listTable.setModel(tmpModel);
      repaint();
   }

   /*--------------------------------------------------------------------*/
   private void doClose()
   {
      boolean success = false;

      dispose();
   }

   /*====================================================================*/
   class ButtonController implements ActionListener
   {
      // Local because it only affects what ResourceModel is VIEWED
      /*------ RCS ---------------------------------------------------------*/
      final String rcsid = "$Id: jHandlerView.java,v 1.3 2000/11/27 19:54:38 gbsmith Exp $";

      /*--------------------------------------------------------------------*/
      public void actionPerformed(ActionEvent ae)
      {
         //String command = ae.getActionCommand();
         Object command = ae.getSource();

         //if(command.compareTo("Close") == closeButton
         if(command == closeButton) doClose();
         else if(command == refreshButton) refresh();
      }
   }

   /*====================================================================*/
   class WindowController extends WindowAdapter
   {
      // *** Perhaps this shouldn't be local after all
      /*------ RCS ---------------------------------------------------------*/
      final String rcsid = "$Id: jHandlerView.java,v 1.3 2000/11/27 19:54:38 gbsmith Exp $";

      /*--------------------------------------------------------------------*/
      public void windowClosing(WindowEvent event)
      {
         doClose();
      }
   }
}

