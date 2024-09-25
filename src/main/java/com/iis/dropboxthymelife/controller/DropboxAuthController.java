package com.iis.dropboxthymelife.controller;

import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.iis.dropboxthymelife.entity.User;
import com.iis.dropboxthymelife.service.DropboxService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class DropboxAuthController {

    @Autowired
    private DropboxService dropboxService;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("accessToken");
        String refreshToken = (String) session.getAttribute("refreshToken");
        Long expiresAt = (Long) session.getAttribute("expiresAt");

        if (accessToken != null && refreshToken != null && expiresAt != null) {
            if (System.currentTimeMillis() < expiresAt) {
                // Access token is valid
                return "dashboard";
            } else {
                // Access token is expired, refresh the token
                try {
                    DbxAuthFinish refreshedCredential = dropboxService.refreshAccessToken(refreshToken);
                    session.setAttribute("accessToken", refreshedCredential.getAccessToken());
                    session.setAttribute("refreshToken", refreshedCredential.getRefreshToken());
                    session.setAttribute("expiresAt", refreshedCredential.getExpiresAt());
                    model.addAttribute("accessToken", refreshedCredential.getAccessToken());
                    return "dashboard";
                } catch (DbxException e) {
                    model.addAttribute("error", "Failed to refresh Dropbox token");
                    return "home";
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            User storedCredential = dropboxService.getStoredCredentials(); // Implement this method
            if (storedCredential != null) {
                if (System.currentTimeMillis() < storedCredential.getExpiresAt()) {
                    session.setAttribute("accessToken", storedCredential.getAccessToken());
                    session.setAttribute("refreshToken", storedCredential.getRefreshToken());
                    session.setAttribute("expiresAt", storedCredential.getExpiresAt());
                    model.addAttribute("accessToken", storedCredential.getAccessToken());
                    return "dashboard";
                } else {
                    try {
                        DbxAuthFinish refreshedCredential = dropboxService.refreshAccessToken(storedCredential.getRefreshToken());
                        session.setAttribute("accessToken", refreshedCredential.getAccessToken());
                        session.setAttribute("refreshToken", refreshedCredential.getRefreshToken());
                        session.setAttribute("expiresAt", refreshedCredential.getExpiresAt());
                        model.addAttribute("accessToken", refreshedCredential.getAccessToken());
                        return "dashboard";
                    } catch (DbxException e) {
                        model.addAttribute("error", "Failed to refresh Dropbox token");
                        return "home";
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return "redirect:/auth";
    }

    @GetMapping("/auth")
    public String authenticate(HttpSession session) {
        String authorizationUrl = dropboxService.getAuthorizationUrl(session);
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/auth/callback")
    public String callback(@RequestParam("code") String code, HttpSession session, Model model) throws DbxException {
        DbxAuthFinish credential = dropboxService.getCredential(code, session);
        if (credential != null) {

            session.setAttribute("accessToken", credential.getAccessToken());
            session.setAttribute("refreshToken", credential.getRefreshToken());
            session.setAttribute("expiresAt", credential.getExpiresAt());

            model.addAttribute("accessToken", credential.getAccessToken());
            model.addAttribute("refreshToken", credential.getRefreshToken());
            model.addAttribute("expiresAt", credential.getExpiresAt());

            dropboxService.storeCredentials(credential);

        } else {
            model.addAttribute("error", "Failed to get Dropbox credentials");
        }
        return "dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}