package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-20
 * @Description: 自定义用户信息服务
 * @Version: 1.0
 */
@Component
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private XcMenuMapper xcMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //将传来的json转为AuthParamsDto
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证的参数不符合要求");
        }

        //获取认证类型
        String authType = authParamsDto.getAuthType();
        //根据认证类型从spring容器中获取认证方法
        //拼接类名
        String beanName = authType + "AuthService";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);

        //调用统一的认证方法
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        //封装返回的信息
        UserDetails userDetails = getUserDetails(xcUserExt);
        return userDetails;
    }

    /**
     * 封装返回的信息
     * @param xcUserExt
     * @return
     */
    private UserDetails getUserDetails(XcUserExt xcUserExt) {
        //根据用户id查询用户权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUserExt.getId());
        String[] auths = {"test"};
        if(xcMenus.size()>0){
            ArrayList<String> permissions = new ArrayList<>();
            for (XcMenu xcMenu : xcMenus) {
                String menuName = xcMenu.getCode();
                permissions.add(menuName);
            }
            auths = permissions.toArray(new String[permissions.size()]);
        }
        String password = xcUserExt.getPassword();
        xcUserExt.setPassword(null);
        String userJson = JSON.toJSONString(xcUserExt);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(auths).build();
        return userDetails;
    }
}
