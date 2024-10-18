package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-15
 * @Description: 课程预览模型类
 * @Version: 1.0
 */
@Data
@ApiModel(value = "CoursePreviewDto",description = "课程预览模型类")
public class CoursePreviewDto {

    /**
     * 课程基本信息，课程营销信息
     */
    @ApiModelProperty("课程基本信息，课程营销信息")
    private CourseBaseInfoDto courseBase;
    /**
     * 课程计划信息
     */
    @ApiModelProperty("课程计划信息")
    private List<TeachPlanDto> teachplans;
    /**
     * 课程师资信息
     */
    @ApiModelProperty("课程师资信息")
    private List<CourseTeacher> courseTeachers;
}
