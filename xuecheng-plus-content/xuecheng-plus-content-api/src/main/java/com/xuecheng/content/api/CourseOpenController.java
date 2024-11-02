package com.xuecheng.content.api;

import com.alibaba.fastjson.JSON;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-15
 * @Description: 课程公开查询接口
 * @Version: 1.0
 */
@Api(value = "CourseOpenController",tags = "课程公开查询接口")
@RestController
@RequestMapping("/open")
public class CourseOpenController {

    @Autowired
    private CoursePublishService coursePublishService;

    /**
     * 根据课程id查询课程预览信息
     * @param courseId
     * @return
     */
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId){
        //封装数据
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        //从缓存中查找
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        if(coursePublish==null){
            //未空直接返回空的coursePreviewDto
            return coursePreviewDto;
        }
        //不为空还需再封装数据
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish,courseBaseInfoDto);
        //课程计划信息
        String teachplan = coursePublish.getTeachplan();
        //转成List<TeachplanDto>
        List<TeachPlanDto> teachPlanDtos = JSON.parseArray(teachplan, TeachPlanDto.class);
        coursePreviewDto.setTeachplans(teachPlanDtos);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        return coursePreviewDto;
    }
}
