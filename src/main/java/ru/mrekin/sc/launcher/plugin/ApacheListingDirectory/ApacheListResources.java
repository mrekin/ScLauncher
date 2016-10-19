package ru.mrekin.sc.launcher.plugin.ApacheListingDirectory;

/**
 * Created by xam on 21.01.2015.
 */
public class ApacheListResources {
    private String resourceURI = "";
    private String text = "";
    private String leaf = "";
    private String lastModified = "";

    public String getResourceURI() {
        return resourceURI;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLeaf() {
        return leaf;
    }

    public void setLeaf(String leaf) {
        this.leaf = leaf;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}