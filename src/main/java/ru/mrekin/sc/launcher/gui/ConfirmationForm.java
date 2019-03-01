package ru.mrekin.sc.launcher.gui;

import ru.mrekin.sc.launcher.core.ISCLogger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.miginfocom.swing.MigLayout;

public class ConfirmationForm extends JDialog  implements ISCLogger {
    private JPanel contentPane;
    private JButton buttonOK = new JButton("OK");
    private JButton buttonCancel = new JButton("Cancel");
    private JTextArea textArea = new JTextArea();
    private boolean result = false;

    public ConfirmationForm() {

        setTitle("Confirmation");
        contentPane = new JPanel((new MigLayout()));

        BufferedImage
                mainIcon = null;
        try {

            mainIcon = ImageIO.read(getClass().getClassLoader().getResource("icon.png"));
        } catch (IOException ioe) {
            log(ioe.getLocalizedMessage());
        }

        setLocationRelativeTo(LauncherGui.getInstance());
        setIconImage(mainIcon);

        setContentPane(contentPane);
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        textArea.setEditable(false);
        contentPane.add(textArea,"wrap");
        contentPane.add(buttonOK, "split 2");
        contentPane.add(buttonCancel,"gapleft push");
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public ConfirmationForm setText(String text){
        textArea.setText(text);
        return this;
    }

    private void onOK() {
        result = true;
        // add your code here
        dispose();
    }

    private void onCancel() {
        result = false;
        // add your code here if necessary
        dispose();
    }

    public boolean launch() {
        pack();
        setVisible(true);
        return result;
    }

}
