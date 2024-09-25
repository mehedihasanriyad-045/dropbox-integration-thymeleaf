package com.iis.dropboxthymelife.helper;



import com.dropbox.core.*;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.oauth.DbxRefreshResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DropboxHelper {

    public static DbxRefreshResult getAccessTokenFromRefreshToken(String accessToken, String refreshToken, String appKey, String appSecret, DbxRequestConfig config) throws DbxException {
        DbxHost host = DbxHost.DEFAULT;
        DbxCredential credential = new DbxCredential(accessToken, System.currentTimeMillis(), refreshToken, appKey, appSecret);

        try {
            return credential.refresh(config, host, null);
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
