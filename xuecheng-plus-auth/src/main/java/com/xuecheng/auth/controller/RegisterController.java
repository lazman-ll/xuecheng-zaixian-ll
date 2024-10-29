package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.dto.RegisterDto;
import com.xuecheng.ucenter.service.RegisterService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-26
 * @Description: 注册接口
 * @Version: 1.0
 */
@RestController
@Slf4j
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    /**
     * 学生注册
     * @param registerDto
     */
    @PostMapping("/register")
    @ApiOperation(value = "注册")
    public void register(@RequestBody RegisterDto registerDto){
        registerService.register(registerDto);
    }
}
