package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;


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

    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto) {
        //1.参数的合法性校验，在接口通过jsr303进行校验
        /*if (StringUtils.isBlank(addCourseDto.getName())) {
//            throw new RuntimeException("课程名称为空");
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
//            throw new RuntimeException("课程分类为空");
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
//            throw new RuntimeException("课程分类为空");
            XueChengPlusException.cast("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getGrade())) {
//            throw new RuntimeException("课程等级为空");
            XueChengPlusException.cast("课程等级为空");
        }
        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
//            throw new RuntimeException("教育模式为空");
            XueChengPlusException.cast("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
//            throw new RuntimeException("适应人群为空");
            XueChengPlusException.cast("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
//            throw new RuntimeException("收费规则为空");
            XueChengPlusException.cast("收费规则为空");
        }*/
        //2.新增课程基础信息
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto,courseBase);//属性名称一致就拷贝
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if(insert<1){
//            throw new RuntimeException("课程基本信息插入错误");
            XueChengPlusException.cast("课程基本信息插入错误");
        }
        //3.新增课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto,courseMarket);
        //课程营销的id和课程基本信息的id一样，mp在插入时会自动返回id
        courseMarket.setId(courseBase.getId());
        //向数据库中保存营销信息
        int saveCourseMarket = saveCourseMarket(courseMarket);
        if(saveCourseMarket<1){
//            throw new RuntimeException("保存营销信息失败");
            XueChengPlusException.cast("保存营销信息失败");
        }
        //从数据库中查询课程的详细详细
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseById(courseBase.getId());
        //返回信息
        return courseBaseInfoDto;
    }


    @Override
    public CourseBaseInfoDto getCourseBaseById(Long courseId) {
        //1.从课程基本信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase==null){
            return null;
        }
        //2.从课程营销信息查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //3.数据封装
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket!=null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }
        //设置分类名称
        //4.从分类表中查询信息
        //4.1查询大分类名称
        CourseCategory courseCategory1 = courseCategoryMapper.selectById(courseBase.getMt());
        //4.2查询小分类名称
        CourseCategory courseCategory2 = courseCategoryMapper.selectById(courseBase.getSt());
        //4.3设置分类名称
        courseBaseInfoDto.setMtName(courseCategory1.getName());
        courseBaseInfoDto.setStName(courseCategory2.getName());
        return courseBaseInfoDto;
    }

    /**
     * 单独写一个方法保存营销信息，逻辑：存在则更新，不存在则插入
     * @param courseMarket
     * @return
     */
    private int saveCourseMarket(CourseMarket courseMarket){
        //1.参数的合法性校验
        String charge = courseMarket.getCharge();
        if (StringUtils.isEmpty(charge)) {
            throw new RuntimeException("收费规则为空");
        }
        if(charge.equals("201001")){
            if(courseMarket.getPrice()==null||courseMarket.getPrice().floatValue()<=0){
//                throw new RuntimeException("课程价格不能为空且必须大于0");
                XueChengPlusException.cast("课程价格不能为空且必须大于0");
            }
        }
        //2.存在则更新，不存在则插入
        //2.1从数据库中查询营销信息
        Long id = courseMarket.getId();
        CourseMarket courseMarketOld = courseMarketMapper.selectById(id);
        //2.2判断是否为空
        if(courseMarketOld==null){
            //2.3为空，直接插入数据库
            return courseMarketMapper.insert(courseMarket);
        }
        //2.4不为空，更新数据库
        return courseMarketMapper.updateById(courseMarket);
    }

    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto editCourseDto) {
        //1.进行数据的业务逻辑校验
        //1.1本机构只能修改本机构的课程
        //根据课程id查询课程以获取该课程对应的机构id
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //进行校验
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }
        //2.封装数据
        //封装课程基本信息
        BeanUtils.copyProperties(editCourseDto,courseBase);

        //封装课程营销信息
        CourseMarket courseMarket=new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        //设置营销信息id
        courseMarket.setId(courseId);

        //设置课程基本信息修改时间
        courseBase.setChangeDate(LocalDateTime.now());
        //TODO 设置课程基本信息修改人

        //3.更新数据库
        //更新课程基本信息
        int update = courseBaseMapper.updateById(courseBase);
        if(update<1){
            XueChengPlusException.cast("更新课程基本信息失败！");
        }

        //更新课程营销信息
        int updateMarket = courseMarketMapper.updateById(courseMarket);
        if (updateMarket<1){
            XueChengPlusException.cast("更新课程营销信息失败");
        }

        //4.查询课程信息
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseById(courseId);

        return courseBaseInfoDto;
    }
}
