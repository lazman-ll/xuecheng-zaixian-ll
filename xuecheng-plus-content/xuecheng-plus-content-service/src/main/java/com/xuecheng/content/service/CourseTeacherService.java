package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;
import sun.util.resources.ga.LocaleNames_ga;

import java.util.List;

public interface CourseTeacherService {
    /**
     * 根据课程id查询教师列表
     * @param courseId
     * @return
     */
    List<CourseTeacher> getCourseTeacherList(Long courseId);


    /**
     * 添加或修改教师
     * @param companyId
     * @param courseTeacher
     * @return
     */
    CourseTeacher saveCourseTeacher(Long companyId, CourseTeacher courseTeacher);

    /**
     * 删除教师
     * @param courseId
     * @param teacherId
     */
    void deleteCourseTeacher(Long companyId,Long courseId, Long teacherId);
}
