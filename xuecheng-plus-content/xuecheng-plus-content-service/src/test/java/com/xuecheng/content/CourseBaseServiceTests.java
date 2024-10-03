package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * 课程基本信息测试类
 */
@SpringBootTest
public class CourseBaseServiceTests {

    @Autowired
    private CourseBaseService courseBaseService;

    @Test
    void testBaseMapper() {

        //查询条件
        QueryCourseParamsDto queryCourseParamsDto =new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");
        queryCourseParamsDto.setAuditStatus("202004");
        //分页查询
        PageParams page=new PageParams(1L,2L);
        PageResult<CourseBase> courseBasePageResult = courseBaseService.queryCourseBasePages(page, queryCourseParamsDto);
        System.out.println("courseBasePageResult = " + courseBasePageResult);
    }
}
