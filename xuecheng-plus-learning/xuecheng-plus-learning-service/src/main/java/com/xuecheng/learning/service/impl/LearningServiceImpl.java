package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.utils.JsonUtil;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTableService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.AbstractDocument;
import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-11-02
 * @Description: 学习相关接口的实现
 * @Version: 1.0
 */
@Service
public class LearningServiceImpl implements LearningService {
    @Autowired
    private MyCourseTableService myCourseTableService;
    @Autowired
    private MediaServiceClient mediaServiceClient;
    @Autowired
    private ContentServiceClient contentServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish==null){
            return RestResponse.validfail("课程不存在");
        }
        //判断该课程是否可以试学
        String teachplanJson = coursepublish.getTeachplan();
        //解析课程计划
        List<Teachplan> teachplans = JsonUtil.jsonToList(teachplanJson, Teachplan.class);
        //遍历所有课程计划
        if (teachplans != null) {
            for (Teachplan teachplan : teachplans) {
                //当前课程计划的id与传入的teachplanId一致，并且isPreview为1，则可以试学
                if("1".equals(teachplan.getIsPreview())&&teachplan.getId().equals(teachplanId)){
                    //远程调用媒资服务获取视频播放地址
                    RestResponse<String> urlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                    if(urlByMediaId!=null){
                        //返回视频播放地址
                        return urlByMediaId;
                    }else {
                        //返回错误信息
                        return RestResponse.validfail("获取播放地址失败");
                    }
                }
            }
        }

        if(StringUtils.isNotEmpty(userId)){
            //判断学习资格
            XcCourseTablesDto courseTablesDto = myCourseTableService.getLearningStatus(userId, courseId);
            if(courseTablesDto==null){
                return RestResponse.validfail("没有学习资格");
            } else if("702002".equals(courseTablesDto.getLearnStatus())){
                return RestResponse.validfail("没有学习资格,因为没有选课或选课后没支付");
            }else if("702003".equals(courseTablesDto.getLearnStatus())){
                return RestResponse.validfail("已过期，需要续期");
            }
            //有资格学习，返回视频的播放地址
            //远程调用媒资服务获取视频播放地址
            RestResponse<String> urlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            if(urlByMediaId!=null){
                return urlByMediaId;
            }else {
                return RestResponse.validfail("获取播放地址失败");
            }
        }
        //用户未登录
        return RestResponse.validfail("请登录后继续学习");
    }
}
