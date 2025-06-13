package com.library.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling login page requests.
 */
@Controller
public class LoginController {
    /**
     * Displays the login page.
     *
     * @return the login view
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
