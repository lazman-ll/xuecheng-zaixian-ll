package com.xuecheng.content.model.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-14
 * @Description: 绑定媒资和课程计划的模型类
 * @Version: 1.0
 */
@ApiModel(value = "BindTeachplanMediaDto",description = "绑定媒资和课程计划的模型类")
@Data
public class BindTeachplanMediaDto {

    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;

    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;
}
