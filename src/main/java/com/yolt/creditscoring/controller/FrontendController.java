package com.yolt.creditscoring.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class FrontendController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        return "forward:/";
    }

    @RequestMapping(method = {RequestMethod.OPTIONS, RequestMethod.GET}, path = {
            "/site-connect-callback",
            "/",
            "/admin",
            "/admin/login",
            "/admin/legal",
            "/admin/privacy-statement",
            "/admin/dashboard",
            "/admin/report/**",
            "/admin/statistics",
            "/admin/settings",
            "/admin/oauth2/callback/github",
            "/admin/oauth2/callback/google",
            "/admin/oauth2/callback/microsoft",
            "/consent/**"
    })
    public String forwardFrontEndPaths() {
        return "forward:/index.html";
    }

}
