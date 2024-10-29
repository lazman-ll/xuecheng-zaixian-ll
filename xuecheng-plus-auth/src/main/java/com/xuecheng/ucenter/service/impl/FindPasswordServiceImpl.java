package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.config.WebSecurityConfig;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.FindPasswordService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-26
 * @Description: 找回密码接口实现
 * @Version: 1.0
 */
@Service
public class FindPasswordServiceImpl implements FindPasswordService {

    @Autowired
    private CheckCodeClient checkCodeClient;
    @Autowired
    private XcUserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public void findPassword(FindPasswordParamsDto findPasswordParamsDto) {

        //首先校验验证码
        boolean verify = checkCodeClient.verify(findPasswordParamsDto.getCheckcodekey(), findPasswordParamsDto.getCheckcode());
        if(!verify){
            throw new RuntimeException("验证码输入错误");
        }
        //判断两次密码是否一致，不一致抛出异常
        if(!findPasswordParamsDto.getPassword().equals(findPasswordParamsDto.getConfirmpwd())){
            throw new RuntimeException("两次密码输入不一致");
        }
        //根据手机号或邮箱查询用户
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StringUtils.isNotEmpty(findPasswordParamsDto.getCellphone()),XcUser::getCellphone, findPasswordParamsDto.getCellphone())
                .eq(StringUtils.isNotEmpty(findPasswordParamsDto.getEmail()),XcUser::getEmail, findPasswordParamsDto.getEmail());
        XcUser xcUser = userMapper.selectOne(queryWrapper);
        if(xcUser==null){
            throw new RuntimeException("用户不存在");
        }
        //找到用户后更新密码
        //密码加密
        String encode = passwordEncoder.encode(findPasswordParamsDto.getPassword());
        System.out.println("encode = " + encode);
        xcUser.setPassword(encode);
        xcUser.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(xcUser);

    }
}
