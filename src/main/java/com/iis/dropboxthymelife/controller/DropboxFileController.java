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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DropboxFileController {

    @Autowired
    private DropboxService dropboxService;

    @GetMapping("/files")
    public String getFilesAndFolders(
            @RequestParam(name = "path", required = false, defaultValue = "") String path,
            HttpSession session,
            Model model) {
        try {
            // Fetch accessToken and refreshToken from session or a secure location
            String accessToken = (String) session.getAttribute("accessToken");
            String refreshToken = (String) session.getAttribute("refreshToken");

            // Call Dropbox service to fetch files and folders at the given path
            List<Metadata> filesAndFolders = dropboxService.getFilesAndFoldersAtPath(accessToken, refreshToken, path);
            List<FileFolderItem> fileFolderItems = new ArrayList<>();
            for (Metadata metadata : filesAndFolders) {
                String type = (metadata instanceof FileMetadata) ? "file" : "folder";
                fileFolderItems.add(new FileFolderItem(metadata.getPathLower(), type));
            }

            // Add files and the current path to the model to be displayed in the Thymeleaf template
            model.addAttribute("filesAndFolders", fileFolderItems);
            model.addAttribute("currentPath", path);

            // Determine the parent folder path for the back button
            String parentPath = !path.isEmpty() && path.contains("/") ? path.substring(0, path.lastIndexOf('/')) : "";
            model.addAttribute("parentPath", parentPath);

        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch files from Dropbox");
        }
        return "dropboxFiles";
    }

    @GetMapping("/read-file")
    public String readFileContent(
            @RequestParam("filePath") String filePath,
            HttpSession session,
            Model model) {
        try {

            String accessToken = (String) session.getAttribute("accessToken");
            String refreshToken = (String) session.getAttribute("refreshToken");

            // Fetch file content
            String fileContent = dropboxService.readFileContent(accessToken, refreshToken, filePath);

            // Add the content to the model to be displayed
            model.addAttribute("fileContent", fileContent);
            model.addAttribute("filePath", filePath);

        } catch (Exception e) {
            model.addAttribute("error", "Unable to read file content");
        }
        return "fileContent"; // New Thymeleaf template to display file content
    }
}

