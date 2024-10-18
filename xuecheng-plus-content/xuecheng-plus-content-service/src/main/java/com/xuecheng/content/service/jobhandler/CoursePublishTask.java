package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.CourseIndex;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-16
 * @Description: 课程发布任务
 * @Version: 1.0
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {


    @Autowired
    private CoursePublishService coursePublishService;
    @Autowired
    private SearchServiceClient searchServiceClient;
    @Autowired
    private CoursePublishMapper coursePublishMapper;
    /**
     * 任务调度入口
     * @throws Exception
     */
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception{
        // 1.分片参数
        //1.1执行器的序号，从0开始
        int shardIndex = XxlJobHelper.getShardIndex();
        //1.2执行器的总数
        int shardTotal = XxlJobHelper.getShardTotal();
        //2.调用抽象类的方法来执行任务
        this.process(shardIndex, shardTotal, "course_publish", 30, 60);
    }


    /**
     * 课程发布任务的执行逻辑
     * @param mqMessage 执行任务内容
     * @return
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        //从mqMessage中拿到课程id
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        //从课程发布表中查询课程信息
        //将课程静态化上传到minio
        generateCourseHtml(mqMessage,courseId);
        //向redis中写入缓存
        saveCourseCache(mqMessage,courseId);
        //向es中插入索引
        saveCourseIndex(mqMessage,courseId);
        //返回true标识任务完成
        return true;
    }

    /**
     * 生成课程静态化页面并上传至文件系统
     * @param mqMessage
     * @param courseId
     */
    public void generateCourseHtml(MqMessage mqMessage,long courseId){
        //取出任务id,查询数据库，判断该任务是否已经完成(任务的幂等性处理)
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService= this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(taskId);
        if(stageOne>0){
            log.debug("静态化页面上传任务已处理完成无需在处理");
        }

        //生成html页面
        File file = coursePublishService.generateCourseHtml(courseId);
        if(file==null){
            log.debug("生成静态页面为空,courseId:{}",courseId);
            XueChengPlusException.cast("生成静态页面为空");
        }
        //将html页面上传至minio
        coursePublishService.uploadCourseHtml(courseId,file);

        //任务完成，更新任务状态为完成
        mqMessageService.completedStageOne(taskId);
    }

    /**
     * 将课程信息缓存至redis
     * @param mqMessage
     * @param courseId
     */
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        //取出任务id,查询数据库，判断该任务是否已经完成(任务的幂等性处理)
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService= this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if(stageTwo>0){
            log.debug("课程信息缓存至redis任务已处理完成无需在处理");
        }


        //任务完成，更新任务状态为完成
        mqMessageService.completedStageTwo(taskId);
    }

    /**
     * 保存课程索引信息
     * @param mqMessage
     * @param courseId
     */
    public void saveCourseIndex(MqMessage mqMessage,long courseId){
        //取出任务id,查询数据库，判断该任务是否已经完成(任务的幂等性处理)
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService= this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(taskId);
        if(stageThree>0){
            log.debug("保存课程索引信息任务已处理完成无需在处理");
        }
        //从课程发布表中查询课程
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //调用远程接口向es中进行添加索引
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        Boolean isSuccess = searchServiceClient.add(courseIndex);
        if(!isSuccess){
            log.debug("远程调用接口向es中添加索引失败,courseId:{}",courseId);
            XueChengPlusException.cast("远程调用接口向es中添加索引失败");
        }


        //任务完成，更新任务状态为完成
        mqMessageService.completedStageThree(taskId);
    }
}
