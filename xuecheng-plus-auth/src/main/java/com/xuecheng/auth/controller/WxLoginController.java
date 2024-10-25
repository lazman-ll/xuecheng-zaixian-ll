package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-20
 * @Description: 微信登录
 * @Version: 1.0
 */
@Controller
@Slf4j
public class WxLoginController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}",code,state);
        //todo 请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库

        XcUser xcUser = new XcUser();
        //暂时硬编写，目的是调试环境
        xcUser.setUsername("t1");
        if(xcUser==null){
            return "redirect:http://www.51xuecheng.cn/error.html";
        }
        String username = xcUser.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username="+username+"&authType=wx";
    }
}
