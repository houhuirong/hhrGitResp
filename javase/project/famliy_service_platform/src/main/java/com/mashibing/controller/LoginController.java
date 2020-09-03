package com.mashibing.controller;

import com.mashibing.bean.TblUserRecord;
import com.mashibing.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: hhr
 * @Date: 2020/9/2 - 09 - 02 - 16:17
 * @Description: com.mashibing.controller.base
 * @version: 1.0
 */
@RestController
@CrossOrigin(origins = "*",allowedHeaders = "*",methods = {},allowCredentials = "true")
public class LoginController {
    @Autowired
    private LoginService loginService;
    @RequestMapping("/auth/2step-code")
    public boolean stepToCode(){
        System.out.println("前端框架000");
        return true;
    }
    @RequestMapping("/auth/login")
    public TblUserRecord login(@RequestParam("username") String username, @RequestParam("password") String password){
        TblUserRecord login = loginService.login(username, password);
        System.out.println(login);
        return login;

    }
}
