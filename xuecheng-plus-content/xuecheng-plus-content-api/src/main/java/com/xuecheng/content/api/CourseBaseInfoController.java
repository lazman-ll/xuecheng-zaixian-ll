package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程基本信息的业务接口
 */
@Api(tags = "课程信息管理接口")

@RestController()
public class CourseBaseInfoController {


    @Autowired
    private CourseBaseService courseBaseService;
    /**
     * 分页查询课程基本信息
     * @param pageParams 分页信息
     * @param queryCourseParamsDto 查询条件
     * @return 分页查询得到的信息
     */
    @ApiOperation("课程信息查询接口")
    @PostMapping ("/course/list")
    public PageResult<CourseBase> list
            (PageParams pageParams,
             @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        PageResult<CourseBase> pageResult = courseBaseService.queryCourseBasePages(pageParams, queryCourseParamsDto);
        return pageResult;

    }

}
