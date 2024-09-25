package com.iis.dropboxthymelife.helper;

import lombok.Data;

@Data
public class FileFolderItem {
    private String id;
    private String path;
    private String type;

    public FileFolderItem(String id, String path, String type) {
        this.path = path;
        this.type = type;
        this.id = id;
    }

    public FileFolderItem(String path, String type) {
        this.path = path;
        this.type = type;
    }

}
