package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-11
 * @Description: 媒体任务service接口
 * @Version: 1.0
 */
public interface MediaFileProcessService {
    /**
     * 获取待处理任务
     * @author 闲人指路
     * @dateTime 20:07 2024/10/11
     * @param shardIndex 当前分片号
     * @param shardTotal 总分片数
     * @param count 获取的任务数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 获取任务的分布式锁
     * @author 闲人指路
     * @param id
     * @return
     */
    public boolean startTask(Long id);

    /**
     * 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author 闲人指路
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
