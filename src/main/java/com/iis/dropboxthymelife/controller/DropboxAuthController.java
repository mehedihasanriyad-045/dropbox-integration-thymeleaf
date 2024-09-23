package com.iis.dropboxthymelife.controller;

import com.dropbox.core.DbxAuthFinish;
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
    public String home() {
        return "home";
    }

    @GetMapping("/auth")
    public String authenticate(HttpSession session) {
        String authorizationUrl = dropboxService.getAuthorizationUrl(session);
        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/auth/callback")
    public String callback(@RequestParam("code") String code, HttpSession session, Model model) {
        DbxAuthFinish credential = dropboxService.getCredential(code, session);
        if (credential != null) {

            session.setAttribute("accessToken", credential.getAccessToken());
            session.setAttribute("refreshToken", credential.getRefreshToken());
            session.setAttribute("expiresAt", credential.getExpiresAt());

            model.addAttribute("accessToken", credential.getAccessToken());
            model.addAttribute("refreshToken", credential.getRefreshToken());
            model.addAttribute("expiresAt", credential.getExpiresAt());
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