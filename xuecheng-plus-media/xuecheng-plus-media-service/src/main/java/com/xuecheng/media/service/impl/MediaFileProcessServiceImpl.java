package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-11
 * @Description: 媒体任务service接口实现
 * @Version: 1.0
 */
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;
    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardIndex,shardTotal,count);
    }

    @Override
    public boolean startTask(Long id) {
        int startTask = mediaProcessMapper.startTask(id);
        return startTask > 0;
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //1.根据任务id获取待处理任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        //2.判断该任务是否存在
        if(mediaProcess== null){
            //2.1不存在，直接返回
            return ;
        }
        //3.判断任务执行是否成功
        if("3".equals(status)){
            //4.任务执行失败
            //4.1直接更新任务状态为失败，增加一次失败次数，更新失败原因
            mediaProcess.setStatus(status);
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1);
            mediaProcess.setErrormsg(errorMsg);
            LambdaQueryWrapper<MediaProcess> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MediaProcess::getId,taskId);
            mediaProcessMapper.update(mediaProcess,queryWrapper);
            //4.2 直接返回
            return ;

        }
        //5.任务执行成功
        //5.1更新媒资表的url为mp4视频的url
        MediaFiles mediaFiles = new MediaFiles();
        mediaFiles.setId(fileId);
        mediaFiles.setUrl(url);
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MediaFiles::getId,fileId);
        mediaFilesMapper.update(mediaFiles,queryWrapper);
        //5.2删除待处理表中的这一条任务
        mediaProcessMapper.deleteById(taskId);
        //5.3将该任务插入历史任务表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        //5.3.1 更新任务状态为成功
        mediaProcessHistory.setStatus(status);
        //5.3.2 更新完成时间
        mediaProcessHistory.setFinishDate(LocalDateTime.now());
        //5.3.3 更新url
        mediaProcessHistory.setUrl(url);
        //5.3.4 插入历史任务表
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
    }
}

