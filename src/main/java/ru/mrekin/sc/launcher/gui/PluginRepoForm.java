package ru.mrekin.sc.launcher.gui;

import ru.mrekin.sc.launcher.core.LauncherConstants;
import ru.mrekin.sc.launcher.core.PluginManager;
import ru.mrekin.sc.launcher.plugin.Plugin;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * Created by xam on 04.02.2015.
 */
public class PluginRepoForm extends JFrame {
    JTable table;
    List<Plugin> plugins;
    JScrollPane jspane;
    JPanel tablePanel;

    public PluginRepoForm() {
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
        setTitle(LauncherConstants.PluginsFormTitle);

        JPanel panel = new JPanel();

        add(panel);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0, 1));

        final JButton installButton = new JButton("Install");
        //JButton updateButton = new JButton("Update");
        final JButton deleteButton = new JButton("Delete");

        installButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!(table.getSelectedRow() == -1)) {
                    if(plugins.get(table.getSelectedRow()).isInstalled()){
                        PluginManager.getInstance().update(plugins.get(table.getSelectedRow()), (String) table.getValueAt(table.getSelectedRow(), 2));
                    }
                    else{
                        PluginManager.getInstance().install(plugins.get(table.getSelectedRow()), (String) table.getValueAt(table.getSelectedRow(), 2));
                        launch();
                        }
                    //PluginManager.getInstance().loadInstalledPlugins();
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
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

        //TODO Before logic implemented
        deleteButton.setEnabled(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                //LauncherGui.getInstance().init();
                LauncherGui.getInstance().launch();
                super.windowClosed(e);
            }
        });


        buttonPanel.add(installButton);
        //buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;

        JPanel east = new JPanel(new GridBagLayout());
        east.add(buttonPanel, gbc);

        // tablePanel = new JPanel();
        //tablePanel.add(new JScrollPane(table));
        //jspane = new JScrollPane(table);
        //tablePanel.add(jspane);

        //    add(tablePanel);


        add(east, BorderLayout.EAST);
        table = new JTable();
        table.getTableHeader().setReorderingAllowed(false);
        jspane = new JScrollPane();
        add(jspane);
        setLocationRelativeTo(null);
    }

    private void launch() {
        remove(jspane);
        //tablePanel = new JPanel();
        //PluginManager.getInstance().load();
        PluginManager.getInstance().loadAvaliablePlugins();
        plugins = PluginManager.getInstance().getAllPlugins();
        createTable(plugins);
        //tablePanel.add();
        jspane = new JScrollPane(table);
        add(jspane);
        pack();
        setVisible(true);
    }

    private void createTable(List<Plugin> plugins) {
        MyTableModel model = new MyTableModel(plugins);
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    }

    private void setRenderer(int columnNum, TableCellRenderer tcr) {

    }


    private class MyTableModel extends AbstractTableModel {
        private List<Plugin> plugins;
        private String[] selectedValues;

        public MyTableModel(List<Plugin> plugins) {
            this.plugins = plugins;
            selectedValues = new String[this.plugins.size()];
        }


        public int getRowCount() {
            return plugins.size();
        }


        public int getColumnCount() {
            //Plugin name
            //Plugin installed version
            //Plugin avaliable version
            //Plugin RepoName version
            //Plugin what ?
            return 4;
        }


        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Plugin name";
                case 1:
                    return "Installed version";
                case 2:
                    return "Avaliable versions";
                case 3:
                    return "Repository";
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
                    return false;
                case 2:
                    return true;
                case 3:
                    return false;
                default:
                    return false;
            }
        }


        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return plugins.get(rowIndex).getPluginName();
                case 1:
                    return plugins.get(rowIndex).getPluginVersion();
                case 2: {
                    String[] array = new String[plugins.get(rowIndex).getPluginVersions().size()];
                    plugins.get(rowIndex).getPluginVersions().keySet().toArray(array);
                    //array = new String[]{"1", "2", "3"};
                    JComboBox box = new JComboBox(array);
                    box.setSelectedIndex(array.length - 1);
                    table.getColumnModel().getColumn(columnIndex).setCellEditor(new DefaultCellEditor(box));
                    DefaultTableCellRenderer renderer =
                            new DefaultTableCellRenderer();
                    renderer.setToolTipText("Click for combo box");
                    renderer.setBackground(Color.LIGHT_GRAY);
                    //                   if (plugins.get(rowIndex).isInstalled() && plugins.get(rowIndex).getPluginVersion().equals(plugins.get(rowIndex).getLatestVersion())) {
                    if (plugins.get(rowIndex).isInstalled() && PluginManager.compareVersions(plugins.get(rowIndex).getPluginVersion(), plugins.get(rowIndex).getLatestVersion()) >= 0) {
                        renderer.setForeground(Color.GREEN);
                    } else {
                        renderer.setForeground(Color.RED);
                    }

                    table.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);

                    if (selectedValues[rowIndex] == null) {
                        selectedValues[rowIndex] = (String) box.getSelectedItem();
                    }
                    return selectedValues[rowIndex];
                }
                case 3:
                    String selectedVer = (String) table.getValueAt(rowIndex, 2);
                    return plugins.get(rowIndex).getPluginVersions().get(selectedVer);
                default:
                    return "Something goes wrong";
            }
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
            selectedValues[row] = (String) value;
            fireTableCellUpdated(row, col);
        }

    }


}
