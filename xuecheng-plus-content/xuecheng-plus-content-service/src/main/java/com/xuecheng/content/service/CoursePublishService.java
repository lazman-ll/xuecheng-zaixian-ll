package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-15
 * @Description: 课程预览、发布service接口
 * @Version: 1.0
 */
public interface CoursePublishService {
    /**
     * 获取课程预览信息
     * @param courseId
     * @return
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * @description 提交审核
     * @param courseId  课程id
     * @return void
     */
    public void commitAudit(Long companyId,Long courseId);

    /**
     * 课程发布
     * @param companyId
     * @param courseId
     */
    void publish (Long companyId,Long courseId);

    /**
     * 课程静态化
     * @param courseId 课程id
     * @return
     */
    public File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     * @param courseId 课程id
     * @param file 静态化文件
     */
    public void  uploadCourseHtml(Long courseId,File file);
}
