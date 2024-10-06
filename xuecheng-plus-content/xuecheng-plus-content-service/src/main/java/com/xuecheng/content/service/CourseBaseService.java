package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;

/**
 * <p>
 * 课程基本信息 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-10-02
 */
public interface CourseBaseService{

    /**
     * 课程信息分页查询
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return 查询结果
     */
    public PageResult<CourseBase> queryCourseBasePages(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程基础信息
     * @param addCourseDto 新增课程基础信息
     * @return 返回的信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId ,AddCourseDto addCourseDto);

    /**
     * 根据id查询课程信息
     * @param courseId 课程id
     * @return 查到的课程信息，用到课程基本信息表和课程营销表
     */
    CourseBaseInfoDto getCourseBaseById(Long courseId);

    /**
     * 修改课程
     * @param companyId 当前用户所属机构id
     * @param editCourseDto 要修改的课程的新信息
     * @return 返回修改完后的课程信息
     */
    CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto editCourseDto);

    /**
     * 删除课程
     * @param courseId
     */
    void deleteCourseBase(Long companyId,Long courseId);
}
