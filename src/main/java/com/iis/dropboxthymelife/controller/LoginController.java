package com.iis.dropboxthymelife.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            clearDropboxSession(session);
            session.invalidate(); // Invalidate the session
        }
        return "redirect:/login?logout=true";
    }

    private void clearDropboxSession(HttpSession session) {
        session.removeAttribute("accessToken");
        session.removeAttribute("refreshToken");
        session.removeAttribute("expiresAt");


    }
}
