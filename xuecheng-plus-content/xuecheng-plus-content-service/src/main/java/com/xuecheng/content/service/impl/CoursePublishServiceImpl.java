package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-15
 * @Description: 课程预览、发布接口实现类
 * @Version: 1.0
 */
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseService courseBaseService;
    @Autowired
    private TeachplanService teachplanService;
    @Autowired
    private CourseTeacherService courseTeacherService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //1.查询课程基本信息和营销信息
        CourseBaseInfoDto courseBaseById = courseBaseService.getCourseBaseById(courseId);
        //2.查询课程计划信息
        List<TeachPlanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        //3.查询课程师资信息
        List<CourseTeacher> courseTeacherList = courseTeacherService.getCourseTeacherList(courseId);
        //4.封装数据
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseById);
        coursePreviewDto.setTeachplans(teachplanTree);
        coursePreviewDto.setCourseTeachers(courseTeacherList);
        return coursePreviewDto;
    }
}
