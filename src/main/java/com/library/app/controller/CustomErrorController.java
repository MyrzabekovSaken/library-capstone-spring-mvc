package com.library.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class CustomErrorController {
    @GetMapping("/403")
    public String error403() {
        return "error/page403";
    }

    @GetMapping("/404")
    public String error404() {
        return "error/page404";
    }
}
