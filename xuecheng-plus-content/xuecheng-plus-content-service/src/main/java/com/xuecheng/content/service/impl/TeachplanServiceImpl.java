package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

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

    /**
     * 确定新增章节或小节的orderBy
     * @param saveTeachplanDto
     * @return
     */
    private Integer getTeachplanCount(SaveTeachplanDto saveTeachplanDto) {
        //查询当前orderBy最大值
       LambdaQueryWrapper<Teachplan> queryWrapper=new LambdaQueryWrapper<>();
       queryWrapper.eq(Teachplan::getCourseId,saveTeachplanDto.getCourseId())
               .eq(Teachplan::getParentid,saveTeachplanDto.getParentid()).orderByDesc(Teachplan::getOrderby)
                       .last("limit 1");
        Teachplan teachplan = teachplanMapper.selectOne(queryWrapper);
        return teachplan.getOrderby();
    }

    @Transactional
    @Override
    public void deleteTeachplan(Long id) {
        //1.根据课程计划id查询课程，判断课程是否存在
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long courseId = teachplan.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase==null){
            XueChengPlusException.cast("课程不存在");
        }
        //2.课程已经发布不能删除课程计划
        if (courseBase.getStatus().equals("203002")){
            XueChengPlusException.cast("课程已发布，无法删除");
        }
        //3.删除章节是，要求该章节下没有小节
        if(teachplan.getGrade()==1){
            //查询是父节点为该课章节的个数
            LambdaQueryWrapper<Teachplan> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,id);
            int count = teachplanMapper.selectCount(queryWrapper);
            if(count>0){
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
            return;
        }
        //4.删除小节时，要求删除与其关联的视频信息
        //4.1删除小节
        int deleteTeachPlan = teachplanMapper.deleteById(id);
        if (deleteTeachPlan<1){
            XueChengPlusException.cast("删除课程小节失败");
        }
        //4.2删除与其关联的媒资信息
        if(teachplan.getMediaType()==null){
            //无媒资信息，直接返回即可
            return;
        }
        LambdaQueryWrapper<TeachplanMedia> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,id);
        int delete = teachplanMediaMapper.delete(queryWrapper);
        if(delete<1){
            XueChengPlusException.cast("删除小节的媒资信息失败");
        }
    }
    @Override
    @Transactional
    public void moveTeachplan(String moveType, Long id) {
        //1.根据查询出当前课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);
        //2.根据该课程计划的parentId查询所有与其同级的课程计划(根据parentId和courseId)
        LambdaQueryWrapper<Teachplan> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,teachplan.getParentid())
                .eq(Teachplan::getCourseId,teachplan.getCourseId())
                .orderByAsc(Teachplan::getOrderby);
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        //2.判断时上移还是下移
        if(moveType.equals("movedown")){
            //下移
            //2.1.判断是否为最后一级，如果是最后一级，则不允许下移
            if(teachplan.equals(teachplans.get(teachplans.size()-1))){
                XueChengPlusException.cast("该课程计划为最后一级，不允许下移");
            }
            //2.2.找到当前课程计划的位置
            int index = getIndexFromList(teachplan, teachplans);
            if(index==-1){
                //未知错误
                XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
            }
            //2.3.找到下一级的课程计划
            Teachplan nextTeachplan = teachplans.get(index+1);
            //2.4.将两者的orderBy交换
            int orderby = teachplan.getOrderby();
            teachplan.setOrderby(nextTeachplan.getOrderby());
            nextTeachplan.setOrderby(orderby);
            //2.5.最后将课程计划更新到数据库中
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(nextTeachplan);
            //2.6直接返回
            return;
        }
        //3.不是下移，是上移
        //3.1.判断是否为第一级，如果是第一级，则不允许上移
        if(teachplan.equals(teachplans.get(0))){
            XueChengPlusException.cast("该课程计划为第一级，不允许上移");
        }
        //3.2.找到当前课程计划的位置
        int index = getIndexFromList(teachplan, teachplans);
        if(index==-1){
            //未知错误
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
        //3.3.找到上一级的课程计划
        Teachplan preTeachplan = teachplans.get(index-1);
        //3.4将两者的orderBy交换
        int orderby = teachplan.getOrderby();
        teachplan.setOrderby(preTeachplan.getOrderby());
        preTeachplan.setOrderby(orderby);
        //3.5.最后将课程计划更新到数据库中
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(preTeachplan);
    }

    /**
     * 获取课程计划在列表中的位置
     * @param teachplan
     * @param teachplans
     * @return
     */
    private int getIndexFromList(Teachplan teachplan, List<Teachplan> teachplans) {
        for (int i = 0; i < teachplans.size(); i++) {
            if(teachplan.equals(teachplans.get(i))){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //根据课程计划id查询课程计划，判断其是否存在，且为二级计划
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null||teachplan.getGrade()!=2){
            XueChengPlusException.cast("课程计划不存在或课程计划为一级课程计划，不允许绑定媒资");
        }
        //1.首先获取课程计划id，根据课程计划id删除该课程已绑定的媒资信息
        LambdaQueryWrapper<TeachplanMedia> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,teachplanId);
        teachplanMediaMapper.delete(queryWrapper);
        //2.创建课程计划-媒资信息对象，并设置对象的值
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto,teachplanMedia);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        //获取课程id
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        //3.将对象插入数据库
        int insert = teachplanMediaMapper.insert(teachplanMedia);
        if(insert<1){
            XueChengPlusException.cast("绑定媒资失败");
        }
    }

    @Override
    public void deleteTeachplanMedia(Long teachplanId, String mediaId) {
        //1.首先根据课程计划id查询课程计划，判断其是否存在
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            //2.不存在，直接返回
            XueChengPlusException.cast("课程计划不存在");
        }
        //3.存在，根据课程计划id和媒资id删除课程计划媒资信息表
        LambdaQueryWrapper<TeachplanMedia> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,teachplanId)
                .eq(TeachplanMedia::getMediaId,mediaId);
        int delete = teachplanMediaMapper.delete(queryWrapper);
        if (delete<1){
            XueChengPlusException.cast("删除课程计划媒资信息失败");
        }
    }
}
