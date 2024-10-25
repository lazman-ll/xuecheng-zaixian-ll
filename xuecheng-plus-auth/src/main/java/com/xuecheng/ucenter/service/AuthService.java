package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-20
 * @Description: 统一认证接口
 * @Version: 1.0
 */
public interface AuthService {

    /**
     * @description 认证方法
     * @param authParamsDto 认证参数
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
