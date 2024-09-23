package com.iis.dropboxthymelife.helper;

public class FileFolderItem {
    private String path;
    private String type;

    public FileFolderItem(String path, String type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }
}
