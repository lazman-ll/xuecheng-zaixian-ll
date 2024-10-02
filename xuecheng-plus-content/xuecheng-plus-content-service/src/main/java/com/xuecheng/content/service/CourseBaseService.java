package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
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

}
