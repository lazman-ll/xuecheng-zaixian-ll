package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseBaseServiceImpl implements CourseBaseService{
    @Autowired
    private CourseBaseMapper courseBaseMapper;


    @Override
    public PageResult<CourseBase> queryCourseBasePages(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //1.创建查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                        CourseBase::getName,queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                        CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                        CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
        //2.创建分页信息
        Page<CourseBase> page=new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //3.查询数据库
        Page<CourseBase> basePage = courseBaseMapper.selectPage(page, queryWrapper);
        //4.封装信息，返回数据
        PageResult<CourseBase> pageResult =
                new PageResult<>(basePage.getRecords(), basePage.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
        return pageResult;
    }
}
