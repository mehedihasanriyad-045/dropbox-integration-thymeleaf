package com.iis.dropboxthymelife.service;

import com.dropbox.core.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

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
            return webAuth.finishFromCode(authorizationCode, redirectUri);
        } catch (DbxException e) {
            e.printStackTrace();
            return null;
        }
    }

//    private DbxClientV2 getClient() {
//        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/springboot-app").build();
//        return new DbxClientV2(config, ACCESS_TOKEN);
//    }
//
//    public List<Metadata> getAllFilesAndFolders() throws Exception {
//        DbxClientV2 client = getClient();
//        ListFolderResult result = client.files().listFolder("");
//        List<Metadata> filesAndFolders = new ArrayList<>();
//
//        while (true) {
//            for (Metadata metadata : result.getEntries()) {
//                filesAndFolders.add(metadata);
//            }
//
//            if (!result.getHasMore()) {
//                break;
//            }
//
//            result = client.files().listFolderContinue(result.getCursor());
//        }
//        return filesAndFolders;
//    }
}