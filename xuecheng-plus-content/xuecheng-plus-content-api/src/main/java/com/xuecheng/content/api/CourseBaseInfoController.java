package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
    //指定权限标识符
    @PreAuthorize("hasAnyAuthority('xc_teachmanager_course_list')")
    public PageResult<CourseBase> list
            (PageParams pageParams,
             @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        //获取机构id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(user!=null){
            if(StringUtils.isNotEmpty(user.getCompanyId())){
                companyId = Long.valueOf(user.getCompanyId());
            }
        }
        PageResult<CourseBase> pageResult = courseBaseService.queryCourseBasePages(companyId,pageParams, queryCourseParamsDto);
        return pageResult;
    }

    /**
     * 新增课程基础信息
     * @param addCourseDto 前端提交的课程基础信息
     * @return 添加后要返回的信息
     */
    @ApiOperation("新增课程基础信息")
    @PostMapping("/course") //@Validated 用于jsr303校验
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(/*ValidationGroups.Insert.class*/) AddCourseDto addCourseDto){
        // 获取用户所属机构的id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(user!=null){
            if(StringUtils.isNotEmpty(user.getCompanyId())){
                companyId = Long.valueOf(user.getCompanyId());
            }
        }
        return courseBaseService.createCourseBase(companyId,addCourseDto);
    }

    /**
     * 根据id查询课程信息
     * @param courseId 课程id
     * @return 查到的课程信息，用到课程基本信息表和课程营销表
     */
    @ApiOperation("根据id查询课程信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable("courseId") Long courseId){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println("user = " + user);
        return courseBaseService.getCourseBaseById(courseId);
    }

    /**
     * 修改课程
     * @param editCourseDto 要修改的课程的新信息
     * @return 返回修改完后的课程信息
     */
    @ApiOperation("修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto){
        //TODO 获取机构id
        Long companyId=1232141425L;
        return courseBaseService.updateCourseBase(companyId,editCourseDto);
    }

    /**
     * 删除课程
     * @param courseId 要删除的课程id
     */
    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourseBase(@PathVariable("courseId") Long courseId){
        //TODO 获取机构id
        Long companyId=1232141425L;
        courseBaseService.deleteCourseBase(companyId,courseId);
    }

}
