package com.iis.dropboxthymelife.helper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.dropbox.core.DbxAuthFinish;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
public class DropboxHelper {

    public static DbxAuthFinish refreshAccessToken(String refreshToken) throws Exception {
        String clientId = "YOUR_CLIENT_ID";  // replace with your Dropbox app's client ID
        String clientSecret = "YOUR_CLIENT_SECRET";  // replace with your Dropbox app's client secret

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.dropbox.com/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=refresh_token&refresh_token=" + refreshToken +
                                "&client_id=" + clientId +
                                "&client_secret=" + clientSecret))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.body());

            String newAccessToken = jsonResponse.get("access_token").asText();
            long expiresIn = jsonResponse.get("expires_in").asLong();
            long expiresAt = System.currentTimeMillis() + (expiresIn * 1000);

            String userId = jsonResponse.get("user_id").asText();
            String teamId = jsonResponse.has("team_id") ? jsonResponse.get("team_id").asText() : null;
            String accountId = jsonResponse.get("account_id").asText();
            String urlState = null;
            String scope = null;

            return new DbxAuthFinish(newAccessToken, expiresIn, refreshToken, userId, teamId, accountId, urlState, scope);
        } else {
            throw new Exception("Failed to refresh token: " + response.body());
        }
    }
}
