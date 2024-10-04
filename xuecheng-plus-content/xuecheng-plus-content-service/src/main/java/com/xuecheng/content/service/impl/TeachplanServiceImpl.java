package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public List<TeachPlanDto> findTeachplanTree(Long courseId) {
        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachPlanDtos;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //1. 数据的逻辑判断
        //此处判断其courseId对应的课程是否存在
        //1.1 根据courseId查找course
        CourseBase courseBase = courseBaseMapper.selectById(saveTeachplanDto.getCourseId());
        //1.2 判断是否存在
        if(courseBase==null){
            //1.3 不存在，直接异常
            XueChengPlusException.cast("当前章节无对应的课程");
        }

        //2.判断是更新还是插入
        //2.1 取出章节的id
        Long id = saveTeachplanDto.getId();
        //2.2 到数据库中查找章节
        Teachplan teachplan = teachplanMapper.selectById(id);
        //2.3 判断是否存在
        if(teachplan!=null){
            //2.4 存在直接更新
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
            return;
        }
        //2.5 不存在，即为添加
        //先为teachplan创建空间
        teachplan=new Teachplan();
        BeanUtils.copyProperties(saveTeachplanDto,teachplan);
        //确定排序字段
        Integer count = getTeachplanCount(saveTeachplanDto);
        teachplan.setOrderby(count+1);

        int insert = teachplanMapper.insert(teachplan);
        if(insert < 1){
            XueChengPlusException.cast("新增章节失败!");
        }
    }

    private Integer getTeachplanCount(SaveTeachplanDto saveTeachplanDto) {
        //查询当前orderBy最大值
       LambdaQueryWrapper<Teachplan> queryWrapper=new LambdaQueryWrapper<>();
       queryWrapper.eq(Teachplan::getCourseId,saveTeachplanDto.getCourseId())
               .eq(Teachplan::getParentid,saveTeachplanDto.getParentid()).orderByDesc(Teachplan::getOrderby)
                       .last("limit 1");
        Teachplan teachplan = teachplanMapper.selectOne(queryWrapper);
        return teachplan.getOrderby();
    }
}
