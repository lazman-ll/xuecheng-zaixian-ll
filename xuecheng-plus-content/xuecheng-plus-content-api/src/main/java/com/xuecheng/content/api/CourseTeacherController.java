package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "师资管理接口")
@RequestMapping("/courseTeacher")
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    /**
     * 根据课程id查询教师列表
     * @param courseId
     * @return
     */
    @GetMapping("/list/{courseId}")
    @ApiOperation("查询教师接口")
    public List<CourseTeacher> getCourseTeacherList(@PathVariable("courseId") Long courseId) {
        return courseTeacherService.getCourseTeacherList(courseId);
    }

    /**
     * 添加或修改教师
     * @param courseTeacher
     * @return
     */
    @PostMapping
    @ApiOperation("添加或修改教师接口")
    public CourseTeacher addCourseTeacher(@RequestBody @Validated(ValidationGroups.Insert.class) CourseTeacher courseTeacher) {
        //TODO 获取机构id
        Long companyId = 1232141425L;
        return courseTeacherService.saveCourseTeacher(companyId,courseTeacher);
    }

    /**
     * 删除教师接口
     * @param courseId
     * @param teacherId
     */
    @DeleteMapping("course/{courseId}/{courseTeacherId}")
    @ApiOperation("删除教师接口")
    public void deleteCourseTeacher(@PathVariable("courseId") Long courseId,
                                    @PathVariable("courseTeacherId") Long teacherId) {
        //TODO 获取机构id
        Long companyId = 1232141425L;
        courseTeacherService.deleteCourseTeacher(companyId,courseId, teacherId);
    }

}
