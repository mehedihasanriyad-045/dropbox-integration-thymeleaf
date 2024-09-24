package com.iis.dropboxthymelife.service;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DropboxService {

    @Value("${dropbox.app.key}")
    private String appKey;

    @Value("${dropbox.app.secret}")
    private String appSecret;

    @Value("${dropbox.redirect.uri}")
    private String redirectUri;

    private static final String SESSION_KEY = "dropbox-auth-csrf-token";

    public String getAuthorizationUrl(HttpSession session) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        DbxAppInfo appInfo = new DbxAppInfo(appKey, appSecret);
        DbxWebAuth webAuth = new DbxWebAuth(config, appInfo);

        DbxSessionStore sessionStore = new DbxSessionStore() {
            @Override
            public String get() {
                return (String) session.getAttribute(SESSION_KEY);
            }

            @Override
            public void set(String value) {
                session.setAttribute(SESSION_KEY, value);
            }

            @Override
            public void clear() {
                session.removeAttribute(SESSION_KEY);
            }
        };

        DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder()
                .withRedirectUri(redirectUri, sessionStore)
                .withTokenAccessType(TokenAccessType.OFFLINE)
                .withScope(Arrays.asList("account_info.read", "files.content.read"))
                .build();

        return webAuth.authorize(authRequest);
    }

    public DbxAuthFinish getCredential(String authorizationCode, HttpSession session) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        DbxAppInfo appInfo = new DbxAppInfo(appKey, appSecret);
        DbxWebAuth webAuth = new DbxWebAuth(config, appInfo);


        try {
            DbxAuthFinish authFinish = webAuth.finishFromCode(authorizationCode, redirectUri);

            session.setAttribute("accessToken", authFinish.getAccessToken());
            session.setAttribute("refreshToken", authFinish.getRefreshToken());
            session.setAttribute("expiresAt", authFinish.getExpiresAt());

            return authFinish;

        } catch (DbxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public DbxClientV2 getClient(String accessToken) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/springboot-app").build();
        return new DbxClientV2(config, accessToken);
    }

    public List<Metadata> getAllFilesAndFolders(String accessToken, String refreshToken) throws Exception {
        DbxClientV2 client = getClient(accessToken);
        ListFolderResult result = client.files().listFolder("");
        List<Metadata> filesAndFolders = new ArrayList<>();

        while (true) {
            filesAndFolders.addAll(result.getEntries());

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
        return filesAndFolders;
    }

    public List<Metadata> getFilesAndFoldersAtPath(String accessToken, String refreshToken, String path) throws Exception {
        DbxClientV2 client = getClient(accessToken);
        ListFolderResult result = client.files().listFolder(path); // Use the path to list files/folders in a specific folder
        List<Metadata> filesAndFolders = new ArrayList<>();

        while (true) {
            filesAndFolders.addAll(result.getEntries());

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
        return filesAndFolders;
    }

    public String readFileContent(String accessToken, String refreshToken, String filePath) throws Exception {
        DbxClientV2 client = getClient(accessToken);

        // Create output stream to store file data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Download file content
            client.files().download(filePath).download(outputStream);

            // Convert the byte array to a string (assuming it's a text file)
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

        } catch (DownloadErrorException e) {
            e.printStackTrace();
            throw new Exception("Error reading file content");
        }
    }

    public InputStream downloadFile(String accessToken, String refreshToken, String filePath) throws Exception {
        DbxClientV2 client = getClient(accessToken);

        try {
            FileMetadata metadata = client.files().download(filePath).getResult();
            return client.files().download(filePath).getInputStream();
        } catch (DownloadErrorException e) {
            throw new Exception("Error downloading file: " + e.getMessage(), e);
        }
    }

}