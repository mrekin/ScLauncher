package ru.mrekin.sc.launcher.core;

import mslinks.LinkTargetIDList;
import mslinks.ShellLink;
import mslinks.ShellLinkException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class ShellLinkEx extends ShellLink {

    public ShellLinkEx() throws IOException, ShellLinkException {
        super();
    }

    public ShellLinkEx(File f) throws IOException, ShellLinkException {
        super(f);
    }

    LinkTargetIDList ids = null;

    public LinkTargetIDList getIdList() throws Exception {

        Field idList = this.getClass().getSuperclass().getDeclaredField("idlist");
        idList.setAccessible(true);
        ids = (LinkTargetIDList) idList.get(this);

        return ids;

    }

    public static ShellLinkEx createLink(String target) {
        ShellLinkEx sl = null;
        try {
            sl = new ShellLinkEx();
            sl.setTarget(target);
        } catch (Exception e) {
            System.out.println(e.toString());
        } finally {
            return sl;
        }

    }

}