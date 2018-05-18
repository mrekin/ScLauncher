package ru.mrekin.sc.launcher.gui;

import org.apache.commons.configuration.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import ru.mrekin.sc.launcher.core.AppManager;
import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.SettingsManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by xam on 04.02.2015.
 */
public class SettingsForm extends JFrame {
    JTable table;
    //List<Plugin> plugins;
    XMLConfiguration configuration;
    JScrollPane jspane;
    JPanel tablePanel;

    public SettingsForm() {
        init();
        launch();
    }

    private void init() {
        //setResizable(false);
        getContentPane().setLayout(new BorderLayout());
        BufferedImage
                mainIcon = null;
        try {

            mainIcon = ImageIO.read(getClass().getClassLoader().getResource("icon.png"));
        } catch (IOException ioe) {
            System.out.println(ioe.getLocalizedMessage());
        }
        setIconImage(mainIcon);
        setTitle(LauncherConstants.SettingsFormTitle);


        JPanel panel = new JPanel();

        add(panel);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1));

        final JButton saveButton = new JButton("Save");
        //JButton updateButton = new JButton("Update");
        //final JButton deleteButton = new JButton("Delete");

        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    if (table.isEditing()) {
                        table.getCellEditor().stopCellEditing();
                    }
                    int rows = table.getModel().getRowCount();

                    for (int i = 0; i < rows; i++) {
                        configuration.setProperty(String.valueOf(table.getModel().getValueAt(i, 0)), table.getModel().getValueAt(i, 1));
                    }

                    configuration.save();
                    dispose();
                } catch (ConfigurationException ce) {
                    ce.printStackTrace();
                }
            }
        });

 /*       deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!(table.getSelectedRow() == -1)) {
                    //TODO need to restart launcher and remove plugin before loading SCL
                    //int row = table.getSelectedRow();
                    //String dirName = plugins.get(row).getPluginSimpleName();
                    //PluginManager.getInstance().remove(plugins.get(row));
                    //plugins.remove(row);
                    //FileDriver.getInstance().deleteFile(SettingsManager.getPropertyByName(LauncherConstants.PluginDirectory)+dirName);
                }
            }
        });
*/

  /*      addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                //LauncherGui.getInstance().init();
                LauncherGui.getInstance().launch();
                super.windowClosed(e);
            }
        });
*/

        //TODO Before logic implemented
        // deleteButton.setEnabled(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                //LauncherGui.getInstance().init();
                AppManager.getInstance().updateAppList();
                //LauncherGui.getInstance().launch();
                System.out.println("!!!! Settings window closing");
                super.windowClosed(e);
            }
        });


        buttonPanel.add(saveButton);
        //buttonPanel.add(updateButton);
        //buttonPanel.add(deleteButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;

        JPanel east = new JPanel(new GridBagLayout());
        east.add(buttonPanel, gbc);

        tablePanel = new JPanel();
        //tablePanel.add(new JScrollPane(table));
        //jspane = new JScrollPane(table);
        //tablePanel.add(jspane);

        add(tablePanel);


        add(east, BorderLayout.EAST);
        table = new JTable();
        table.getTableHeader().setReorderingAllowed(false);

        setLocationRelativeTo(null);
    }

    private void launch() {
        //remove(tablePanel);
        //tablePanel = new JPanel();

        configuration = SettingsManager.getInstance().getXmlConfiguration();
        createTable(configuration);
        jspane = new JScrollPane(table);
        jspane.setBorder(new LineBorder(Color.green));
        //jspane.setPreferredSize(table.getMaximumSize());
        //jspane.size
        //tablePanel.add(jspane);
        //tablePanel.setBorder(new LineBorder(Color.red));
        add(jspane);
        pack();
        setVisible(true);
    }

    private void createTable(XMLConfiguration configuration) {
        MyTableModel model = new MyTableModel(configuration);
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    }

    private void setRenderer(int columnNum, TableCellRenderer tcr) {

    }


    private class MyTableModel extends AbstractTableModel {
        private ArrayList<String[]> properties = new ArrayList<String[]>(1);
        //private String[] selectedValues;

        public MyTableModel(XMLConfiguration configuration) {

            Iterator<String> iter = configuration.getKeys();
            while (iter.hasNext()) {
                String nextElement = iter.next();
                System.out.println(nextElement + ": " + configuration.getProperty(nextElement));
                this.properties.add(new String[]{nextElement, configuration.getProperty(nextElement).toString()});
            }
            //selectedValues = new String[this.plugins.size()];


        }


        public int getRowCount() {
            return properties.size();
        }


        public int getColumnCount() {
            //name
            //value

            //Plugin what ?
            return 2;
        }


        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Name";
                case 1:
                    return "Value";
                default:
                    return "Unknown column";
            }

        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {

            return getValueAt(0, columnIndex).getClass();

        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return false;
                case 1:
                    return true;
                default:
                    return false;
            }
        }


        public Object getValueAt(int rowIndex, int columnIndex) {
            return properties.get(rowIndex)[columnIndex]; //plugins.get(rowIndex).getPluginName();
        }

        /*
                @Override
                public void addTableModelListener(TableModelListener l) {

                }
        */
        @Override
        public void removeTableModelListener(TableModelListener l) {

        }

        public void setValueAt(Object value, int row, int col) {
            String[] property = properties.get(row);
            property[col] = String.valueOf(value);
            properties.set(row, property);
            fireTableCellUpdated(row, col);
        }

    }


}
