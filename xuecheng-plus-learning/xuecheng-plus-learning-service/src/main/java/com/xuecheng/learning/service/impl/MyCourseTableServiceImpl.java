package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTableService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-29
 * @Description: 选课相关接口的实现
 * @Version: 1.0
 */
@Service
public class MyCourseTableServiceImpl implements MyCourseTableService {

    @Autowired
    private XcChooseCourseMapper chooseCourseMapper;
    @Autowired
    private XcCourseTablesMapper courseTablesMapper;
    @Autowired
    private ContentServiceClient contentServiceClient;

    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        //远程调用内容管理服务查询器收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish==null){
            XueChengPlusException.cast("课程不存在");
        }
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        if("201000".equals(coursepublish.getCharge())){
            //免费课程，向选课表插入数据，向课程表插入数据
            //向选课表插入数据
            xcChooseCourse = addFreeCourse(userId, coursepublish);
            //向课程表插入数据
            XcCourseTables xcCourseTables = addCourseTables(xcChooseCourse);
        }else {
            //收费课程，向选课表插入数据
            xcChooseCourse = addChargeCourse(userId, coursepublish);
        }

        //将学生的学习资格写入XcChooseCourseDto中并返回
        XcChooseCourseDto courseTablesDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse,courseTablesDto);
        courseTablesDto.setLearnStatus(getLearningStatus(userId, courseId).getLearnStatus());
        return courseTablesDto;

    }

    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        //查询我的课程表，如果查不到说明未选课
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getCourseId,courseId);
        XcChooseCourse xcChooseCourse = chooseCourseMapper.selectOne(queryWrapper);
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        if(xcChooseCourse==null){
            //未选课
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        //若查到了，还要判断是否过期
        BeanUtils.copyProperties(xcChooseCourse,xcCourseTablesDto);
        if (xcChooseCourse.getValidtimeEnd().isBefore(LocalDateTime.now())) {
            //已过期
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
        //未过期,可正常学习
        xcCourseTablesDto.setLearnStatus("702001");
        return xcCourseTablesDto;
    }

    /**
     * 免费课程,向选课记录表添加数据
     * @param userId
     * @param coursepublish
     */
    private XcChooseCourse  addFreeCourse(String userId, CoursePublish coursepublish) {
        //首先判断选课记录表中是否已经有该记录
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        Long courseId = coursepublish.getId();
        queryWrapper
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getCourseId,courseId)
                //免费课程
                .eq(XcChooseCourse::getOrderType,"700001")
                //选课成功
                .eq(XcChooseCourse::getStatus,"701001");
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses.size() > 0){
            //课程记录已存在,直接返回第一个
            return xcChooseCourses.get(0);
        }
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        BeanUtils.copyProperties(coursepublish,xcChooseCourse);
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        //免费课程设置课程价格为0
        xcChooseCourse.setCoursePrice((float) 0);
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        //免费课程
        xcChooseCourse.setOrderType("70001");
        //选课成功
        xcChooseCourse.setStatus("701001");
        int insert = chooseCourseMapper.insert(xcChooseCourse);
        if(insert<1){
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }

    /**
     * 向课程表添加数据
     * @param xcChooseCourse
     * @return
     */
    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse){
        //判断选课状态是否为成功
        if(!"701001".equals(xcChooseCourse.getStatus())){
            XueChengPlusException.cast("选课状态不正确，添加课程表失败");
        }
        //首先判断课程表中是否已经有该数据
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(XcCourseTables::getUserId,xcChooseCourse.getUserId())
                .eq(XcCourseTables::getCourseId,xcChooseCourse.getCourseId());
        XcCourseTables xcCourseTables = courseTablesMapper.selectOne(queryWrapper);
        if(xcCourseTables!=null){
            return xcCourseTables;
        }
        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse,xcCourseTables);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTables.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTables.setUpdateDate(LocalDateTime.now());
        int insert = courseTablesMapper.insert(xcCourseTables);
        if(insert<1){
            XueChengPlusException.cast("添加课程表失败");
        }
        return xcCourseTables;
    }

    /**
     * 添加收费课程
     * @param userId
     * @param coursepublish
     * @return
     */
    public XcChooseCourse addChargeCourse(String userId,CoursePublish coursepublish){
        //判断选课记录表中是否已经有该记录
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        //课程id
        Long courseId = coursepublish.getId();
        queryWrapper
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getCourseId,courseId)
                //收费课程
                .eq(XcChooseCourse::getOrderType,"700002")
                //选课状态为待支付
                .eq(XcChooseCourse::getStatus,"701002");
        List<XcChooseCourse> xcChooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if(xcChooseCourses.size() > 0){
            //课程记录已存在,直接返回第一个
            return xcChooseCourses.get(0);
        }

        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        BeanUtils.copyProperties(coursepublish,xcChooseCourse);
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        //免费课程
        xcChooseCourse.setOrderType("70002");
        //选课成功
        xcChooseCourse.setStatus("701002");
        int insert = chooseCourseMapper.insert(xcChooseCourse);
        if(insert<1){
            XueChengPlusException.cast("添加选课记录失败");
        }
        return xcChooseCourse;
    }
}
