package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;
import com.xuecheng.ucenter.service.FindPasswordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-26
 * @Description: 找回密码接口
 * @Version: 1.0
 */
@RestController
@Slf4j
public class FindPasswordController {

    @Autowired
    private FindPasswordService findPasswordService;

    /**
     * 找回密码
     * @param findPasswordParamsDto
     */
    @PostMapping("/findpassword")
    @ApiOperation(value = "找回密码")
    public void findPassword(@RequestBody FindPasswordParamsDto findPasswordParamsDto){
        findPasswordService.findPassword(findPasswordParamsDto);
    }

}
