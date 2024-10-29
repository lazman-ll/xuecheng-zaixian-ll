package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.RegisterDto;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-26
 * @Description: 注册service接口
 * @Version: 1.0
 */
public interface RegisterService {
    /**
     * 注册
     * @param registerDto
     */
    void register(RegisterDto registerDto);
}
