package com.zj.casclient4.controller;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class TestController {


    @RequestMapping("/test")
    public String test(HttpServletRequest request) {

        AttributePrincipal principal = (AttributePrincipal)request.getUserPrincipal();
        String loginName = principal.getName();
        return loginName;
    }

    /**
     * 退出
     *
     * @param request
     * @return
     */
    @RequestMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
//        session.invalidate();
        return "redirect:http://localhost:8443/cas/logout?service=http://localhost:9004/client4/logoutSuccess";

    }
}
