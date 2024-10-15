package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return coursePublishService.getCoursePreviewInfo(courseId);
    }
}
