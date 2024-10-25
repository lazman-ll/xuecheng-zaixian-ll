package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-20
 * @Description: 账号密码认证实现接口
 * @Version: 1.0
 */
@Service("passwordAuthService")
@Slf4j
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //获取用户名
        String username = authParamsDto.getUsername();

        //检验验证码
        //获取前端输入的验证码
        String checkcode = authParamsDto.getCheckcode();
        //获取验证码的key
        String checkcodekey = authParamsDto.getCheckcodekey();
        if(StringUtils.isEmpty(checkcode)|| StringUtils.isEmpty(checkcodekey)){
            throw new RuntimeException("请输入验证码");
        }

        //远程调用验证码服务校验验证码
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if(verify==null||!verify){
            throw new RuntimeException("验证码输入错误!");
        }

        //根据username查询数据库
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcUser::getUsername,username);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        //校验账号是否存在
        if(xcUser==null){
            //不存在，直接返回空(null)
            log.info("用户不存在");
            throw new RuntimeException("账号不存在");
        }


        //存在，校验密码是否正确
        //获取正确的密码
        String password = xcUser.getPassword();
        //获取用户输入的密码
        String pwd_input = authParamsDto.getPassword();
        //校验密码
        boolean matches = passwordEncoder.matches(pwd_input, password);
        if(!matches){
            log.info("密码不正确");
            throw new RuntimeException("账号或密码错误");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }
}
