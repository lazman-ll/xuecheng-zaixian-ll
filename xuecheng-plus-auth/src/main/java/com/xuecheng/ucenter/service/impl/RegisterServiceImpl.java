package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.RegisterService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-26
 * @Description: 注册接口实现类
 * @Version: 1.0
 */
@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private XcUserMapper userMapper;
    @Autowired
    private CheckCodeClient checkCodeClient;
    @Autowired
    private XcUserRoleMapper userRoleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void register(RegisterDto registerDto) {
        //校验验证码
         boolean verify = checkCodeClient.verify(registerDto.getCheckcodekey(), registerDto.getCheckcode());
        if(!verify){
            XueChengPlusException.cast("验证码输入错误");
        }
        //判断两次密码是否一致，不一致抛出异常
        if(!registerDto.getPassword().equals(registerDto.getConfirmpwd())){
            XueChengPlusException.cast("两次密码输入不一致");
        }
        //首先通过手机查询用户是否存在
        String cellphone = registerDto.getCellphone();
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(cellphone),XcUser::getCellphone,cellphone);
        XcUser xcUser = userMapper.selectOne(queryWrapper);
        if(xcUser!=null){
            XueChengPlusException.cast("该手机号码已经注册");
        }
        xcUser = new XcUser();
        //首先封装学生信息
        BeanUtils.copyProperties(registerDto,xcUser);
        xcUser.setCreateTime(LocalDateTime.now());
        xcUser.setStatus("1");
        xcUser.setName("学生");
        xcUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        xcUser.setUtype("101001");
        //插入数据库
        int insert = userMapper.insert(xcUser);
        if(insert<=0){
            XueChengPlusException.cast("注册信息插入失败");
        }
        //向用户角色表中添加该用户为学生的信息
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setUserId(xcUser.getId());
        //学生角色
        xcUserRole.setRoleId("17");
        xcUserRole.setCreateTime(LocalDateTime.now());
        //插入数据库
        int insert1 = userRoleMapper.insert(xcUserRole);
        if(insert1<=0){
            XueChengPlusException.cast("角色信息插入失败");
        }
    }
}
