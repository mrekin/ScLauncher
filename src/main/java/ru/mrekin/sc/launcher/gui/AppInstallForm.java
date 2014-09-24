package ru.mrekin.sc.launcher.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by xam on 21.09.2014.
 */
public class AppInstallForm extends JFrame {

    int current = 0;
    int maximum = 100;
    JProgressBar progressBar;
    JLabel label;

    public AppInstallForm() {

        setSize(new Dimension(150, 50));
        setResizable(false);
        setLocationByPlatform(true);
        setUndecorated(true);
        setAlwaysOnTop(true);

        setTitle("Installing...");

        JPanel panel = new JPanel();
        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setStringPainted(true);
        progressBar.setMaximumSize(new Dimension(100, 20));

        label = new JLabel("test");

        panel.add(progressBar);
        panel.add(label);
        add(panel);
        setVisible(true);
        //dispose();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


    }

    public void setValues(int current, int maximum) {
        this.current = current;
        this.maximum = maximum;

    }

    public void update() {
        progressBar.setMaximum(maximum);
        progressBar.setValue(current);
        label.setText(current + "/" + maximum + " files");
        //progressBar.updateUI();
    }
}
