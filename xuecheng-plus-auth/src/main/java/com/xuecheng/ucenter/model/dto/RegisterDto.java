package com.xuecheng.ucenter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-26
 * @Description: 注册的请求类
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
    private String cellphone;
    private String username;
    private String email;
    private String nickName;
    private String password;
    private String confirmpwd;
    private String checkcodekey;
    private String checkcode;
}
