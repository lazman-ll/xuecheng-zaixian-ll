package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-15
 * @Description: 课程预览、发布接口实现类
 * @Version: 1.0
 */
@Service
@Slf4j
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
    @Autowired
    private CoursePublishMapper coursePublishMapper;
    @Autowired
    private MqMessageService mqMessageService;
    @Autowired
    private MediaServiceClient mediaServiceClient;

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
    @Transactional(rollbackFor = Exception.class)
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long companyId, Long courseId) {
        //判断是否为本机构的课程
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只能发布本机构的课程！！！");
        }
        //查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre==null){
            XueChengPlusException.cast("课程无审核记录，无法发布！！！");
        }
        //判断审核是否已经通过
        if(!"202004".equals(coursePublishPre.getStatus())){
            XueChengPlusException.cast("课程没有审核通过，不能发布！！！");
        }
        //向课程发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        //先查询，看是否已存在
        CoursePublish coursePublishOld = coursePublishMapper.selectById(courseId);
        if(coursePublishOld==null){
            //不存在，插入
            coursePublishMapper.insert(coursePublish);
        }else {
            //存在，更新
            coursePublishMapper.updateById(coursePublish);
        }
        // 向消息表写入数据
        saveCoursePublishMessage(courseId);

        //将预发布表中的数据删除
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * @description 保存消息表记录
     * @param courseId  课程id
     * @return void
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        Configuration configuration = new Configuration(Configuration.getVersion());
        File htmlFile=null;
        try {
            //获取classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            //指定目录
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //指定编码
            configuration.setDefaultEncoding("utf-8");
            //获取模板
            Template template = configuration.getTemplate("course_template.ftl");
            //查询课程的信息，作为模型数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            HashMap<String, Object> modelMap = new HashMap<>();
            //设置模型数据
            modelMap.put("model", coursePreviewInfo);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, modelMap);
            //使用流将html写入文件
            //输入流
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            htmlFile=File.createTempFile("coursePublish", ".html");
            //输出流
            FileOutputStream outputStream =
                    new FileOutputStream(htmlFile);
            //使用流将html写入文件
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("页面静态化出错，课程id：{}，异常信息：{}",courseId,e.getMessage());
            e.printStackTrace();
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        //将file转化为MultipartFile
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        //远程调用将文件上传到minio
        String upload = mediaServiceClient.upload(multipartFile, "course/"+courseId+".html");
        if(StringUtils.isEmpty(upload)){
            log.debug("远程调用走了降级逻辑，courseId：{}",courseId);
            XueChengPlusException.cast("上传静态文件过程中异常");
        }
    }
}
