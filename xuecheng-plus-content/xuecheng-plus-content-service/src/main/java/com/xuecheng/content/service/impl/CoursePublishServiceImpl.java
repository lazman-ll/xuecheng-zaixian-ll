package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-15
 * @Description: 课程预览、发布接口实现类
 * @Version: 1.0
 */
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseService courseBaseService;
    @Autowired
    private TeachplanService teachplanService;
    @Autowired
    private CourseTeacherService courseTeacherService;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //1.查询课程基本信息和营销信息
        CourseBaseInfoDto courseBaseById = courseBaseService.getCourseBaseById(courseId);
        //2.查询课程计划信息
        List<TeachPlanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        //3.查询课程师资信息
        List<CourseTeacher> courseTeacherList = courseTeacherService.getCourseTeacherList(courseId);
        //4.封装数据
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseById);
        coursePreviewDto.setTeachplans(teachplanTree);
        coursePreviewDto.setCourseTeachers(courseTeacherList);
        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        //查询课程基本信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseService.getCourseBaseById(courseId);
        if(courseBaseInfoDto==null){
            XueChengPlusException.cast("找不到该课程！！！");
        }
        //本机构只能提交本机构的课程
        if(!companyId.equals(courseBaseInfoDto.getCompanyId())){
            XueChengPlusException.cast("本机构只能提交本机构的课程！！！");
        }
        //课程状态为已提交的不可提交直接返回
        if ("202003".equals(courseBaseInfoDto.getStatus())) {
            XueChengPlusException.cast("课程已提交，请等待审核！！！");
        }
        //课程信息填写不完全的也不能提交
        if (StringUtils.isEmpty(courseBaseInfoDto.getPic())){
            XueChengPlusException.cast("请上传课程图片！！！");
        }
        //查询课程计划
        List<TeachPlanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if(teachplanTree==null || teachplanTree.size()==0){
            XueChengPlusException.cast("请编写课程计划！！！");
        }
        //将上述数据封装
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfoDto,coursePublishPre);
        //查询营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //计划信息
        coursePublishPre.setMarket(courseMarketJson);
        String teachplanJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanJson);
        //师资信息
        List<CourseTeacher> courseTeacherList = courseTeacherService.getCourseTeacherList(courseId);
        String courseTeacherJson = JSON.toJSONString(courseTeacherList);
        coursePublishPre.setTeachers(courseTeacherJson);
        //修改状态为已提交
        coursePublishPre.setStatus("202003");
        //设置提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //查询预发布表，有该记录则更新，否则新增
        CoursePublishPre coursePublishPreOld = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreOld==null){
            //插入到课程预发布表
            coursePublishPreMapper.insert(coursePublishPre);
        }else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //修改课程基本信息表的状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }
}
