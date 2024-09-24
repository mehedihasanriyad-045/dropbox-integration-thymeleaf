package com.iis.dropboxthymelife.controller;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.iis.dropboxthymelife.helper.FileFolderItem;
import com.iis.dropboxthymelife.service.DropboxService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("fileController")
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

            // Extract file extension to determine the type
            String fileType = getFileType(filePath);

            // If it's a text file, fetch its content
            String fileContent = "";
            if (fileType.equals("text")) {
                fileContent = dropboxService.readFileContent(accessToken, refreshToken, filePath);
            }

            // Add attributes to model
            model.addAttribute("filePath", filePath);
            model.addAttribute("fileContent", fileContent);
            model.addAttribute("fileType", fileType);

        } catch (Exception e) {
            model.addAttribute("error", "Unable to read file content");
        }
        return "fileContent";
    }

    private String getFileType(String filePath) {
        if (filePath.endsWith(".txt") || filePath.endsWith(".md")) {
            return "text";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png") || filePath.endsWith(".gif")) {
            return "image";
        } else if (filePath.endsWith(".mp4") || filePath.endsWith(".avi") || filePath.endsWith(".mov")) {
            return "video";
        } else if (filePath.endsWith(".pdf")) {
            return "pdf";
        } else {
            return "unsupported";
        }
    }

    @GetMapping("/download-file")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("filePath") String filePath, HttpSession session) {
        try {
            String accessToken = (String) session.getAttribute("accessToken");
            String refreshToken = (String) session.getAttribute("refreshToken");

            // Get file stream from Dropbox service
            InputStream fileStream = dropboxService.downloadFile(accessToken, refreshToken, filePath);

            // Set headers and prepare the response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filePath.substring(filePath.lastIndexOf("/") + 1));
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(fileStream));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public String getFileIcon(String filePath) {
        if (filePath.endsWith(".pdf")) {
            return "fas fa-file-pdf pdf-icon";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png") || filePath.endsWith(".gif")) {
            return "fas fa-file-image image-icon";
        } else if (filePath.endsWith(".mp4") || filePath.endsWith(".mkv") || filePath.endsWith(".avi")) {
            return "fas fa-file-video video-icon";
        } else if (filePath.endsWith(".mp3") || filePath.endsWith(".wav")) {
            return "fas fa-file-audio audio-icon";
        } else if (filePath.endsWith(".doc") || filePath.endsWith(".docx")) {
            return "fas fa-file-word word-icon";
        } else if (filePath.endsWith(".xls") || filePath.endsWith(".xlsx")) {
            return "fas fa-file-excel excel-icon";
        } else {
            return "fas fa-file-alt file-icon"; // Default icon for other file types
        }
    }

}

