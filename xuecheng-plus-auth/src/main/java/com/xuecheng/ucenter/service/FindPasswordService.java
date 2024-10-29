package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-26
 * @Description: 找回密码service接口
 * @Version: 1.0
 */
public interface FindPasswordService {
    /**
     * 找回密码
     * @param findPasswordParamsDto
     */
    void findPassword(FindPasswordParamsDto findPasswordParamsDto);
}
