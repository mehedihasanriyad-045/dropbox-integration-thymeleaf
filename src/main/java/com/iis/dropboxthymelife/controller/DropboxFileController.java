package com.iis.dropboxthymelife.controller;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.iis.dropboxthymelife.helper.FileFolderItem;
import com.iis.dropboxthymelife.service.DropboxService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DropboxFileController {

    @Autowired
    private DropboxService dropboxService;

    @GetMapping("/files")
    public String getFilesAndFolders(HttpSession session, Model model) {
        try {
            // Fetch accessToken and refreshToken from session or a secure location
            String accessToken = (String) session.getAttribute("accessToken");
            String refreshToken = (String) session.getAttribute("refreshToken");

            // Call Dropbox service to fetch files and folders
            List<Metadata> filesAndFolders = dropboxService.getAllFilesAndFolders(accessToken, refreshToken);
            List<FileFolderItem> fileFolderItems = new ArrayList<>();
            for (Metadata metadata : filesAndFolders) {
                String type = (metadata instanceof FileMetadata) ? "file" : "folder";
                fileFolderItems.add(new FileFolderItem(metadata.getPathLower(), type));
            }

            // Add files to the model to be displayed in the Thymeleaf template
            model.addAttribute("filesAndFolders", fileFolderItems);

        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch files from Dropbox");
        }
        return "dropboxFiles";
    }
}
