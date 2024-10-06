package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        //1.根据课程id查询老师列表
        LambdaQueryWrapper<CourseTeacher> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }


    @Override
    public CourseTeacher saveCourseTeacher(Long companyId, CourseTeacher courseTeacher) {
        //1.业务逻辑校验，只允许向机构自己的课程中添加或修改老师
        checkCompanyId(companyId, courseTeacher.getCourseId());
        //2.判断是否已经存在
        LambdaQueryWrapper<CourseTeacher> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getId,courseTeacher.getId());
        CourseTeacher courseTeacherOld = courseTeacherMapper.selectOne(queryWrapper);
        if (courseTeacherOld!=null){
            //2.1已经存在，修改师资信息
            int update = courseTeacherMapper.updateById(courseTeacher);
            //2.2.判断是否修改成功
            if(update< 1){
                XueChengPlusException.cast("修改教师信息失败");
            }
            return courseTeacher;
        }
        //3不存在，添加师资信息
        //3.1course_id和teacher_name不能同时相等
        //即一个老师不能重复的与一个课程对应
        queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseTeacher.getCourseId())
                .eq(CourseTeacher::getTeacherName,courseTeacher.getTeacherName());
        CourseTeacher teacher = courseTeacherMapper.selectOne(queryWrapper);
        if(teacher!=null){
            XueChengPlusException.cast("该老师已添加");
        }
        //3.2设置创建时间
        courseTeacher.setCreateDate(LocalDateTime.now());
        //3.3添加师资信息
        int insert = courseTeacherMapper.insert(courseTeacher);
        //3.4.判断是否插入成功
        if(insert< 1){
            XueChengPlusException.cast("添加教师信息失败");
        }
        //4.返回添加的课程老师对象
        return courseTeacher;
    }

    private void checkCompanyId(Long companyId, Long courseId) {
        //1.2根据课程id查询课程
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //1.3判断课程是否属于本机构
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能变更本机构的课程");
        }
    }

    @Override
    public void deleteCourseTeacher(Long companyId,Long courseId, Long teacherId) {
        //1.业务逻辑校验，只允许向机构自己的课程中删除老师
        checkCompanyId(companyId, courseId);
        //2.删除
        LambdaQueryWrapper<CourseTeacher> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getId,teacherId)
                .eq(CourseTeacher::getCourseId,courseId);
        int delete = courseTeacherMapper.delete(queryWrapper);
        //2.1判断是否删除成功
        if(delete< 1){
            XueChengPlusException.cast("删除教师信息失败");
        }
    }
}
