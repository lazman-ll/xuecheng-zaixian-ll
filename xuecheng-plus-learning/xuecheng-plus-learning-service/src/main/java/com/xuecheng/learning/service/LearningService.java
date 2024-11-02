package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-11-02
 * @Description: 学习相关service接口
 * @Version: 1.0
 */
public interface LearningService {
    /**
     * 获取视频播放信息
     * @param userId
     * @param courseId
     * @param teachplanId
     * @param mediaId
     * @return
     */
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
