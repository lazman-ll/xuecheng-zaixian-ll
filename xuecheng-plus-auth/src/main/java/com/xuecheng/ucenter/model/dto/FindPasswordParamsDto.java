package com.xuecheng.ucenter.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-26
 * @Description: 找回密码的dto
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindPasswordParamsDto {

    private String cellphone;
    private String checkcode;
    private String password;
    private String email;
    private String confirmpwd;
    private String checkcodekey;
}
