package ru.mrekin.sc.launcher.core;

import mslinks.LinkTargetIDList;
import mslinks.ShellLink;
import mslinks.ShellLinkException;
import ru.mrekin.sc.launcher.gui.ApplicationPrepareLinkForm;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;


public class AppDropTarget extends DropTarget implements ISCLogger {
    private static AppDropTarget instance;
    private JFrame jform = null;

    public static AppDropTarget getInstance() {
        if (instance == null) {
            return new AppDropTarget();
        } else {
            return instance;
        }
    }

    public void setAppPrepareLinkForm(JFrame jform) {
        this.jform = jform;
    }

    public synchronized void drop(DropTargetDropEvent evt) {
        try {
            ShellLinkEx link = null;
            evt.acceptDrop(DnDConstants.ACTION_COPY);
            List<File> droppedFiles = (List<File>)
                    evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            for (File file : droppedFiles) {
                log("Dropped file: " + file.getAbsolutePath());
                if (file.getAbsolutePath().endsWith(".lnk")) {
                    link = new ShellLinkEx(file);
                    log(link.resolveTarget());
                } else {
                    link = ShellLinkEx.createLink(file.getAbsolutePath());
                    log(link.resolveTarget());
                }
                if (jform == null || !jform.isVisible()) {
                    jform = new ApplicationPrepareLinkForm(link);
                }
                jform.setEnabled(true);
            }
        } catch (ShellLinkException se) {
            se.printStackTrace();
            log(se.toString());
        } catch (Exception ex) {
            log(ex.toString(), "ERROR");
        }
    }

    /*
        private static void log(String msg) {
            SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(), "INFO", msg);
        }
    */
    private static void log(String msg, String level) {
        SCLogger.getInstance().log(MethodHandles.lookup().lookupClass().getName(), level, msg);
    }


}
