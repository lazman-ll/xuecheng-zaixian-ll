package com.xuecheng.ucenter.service.impl;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.stereotype.Service;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-20
 * @Description: 微信扫码认证
 * @Version: 1.0
 */
@Service("wxAuthService")
public class WxAuthServiceImpl implements AuthService {
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        return null;
    }
}
