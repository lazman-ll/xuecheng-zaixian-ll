package com.xuecheng.media.service.jobhandler;

import com.baomidou.mybatisplus.extension.api.R;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 视频处理任务类
 * @author 闲人指路
 */
@Slf4j
@Component
public class VideoTask {

    @Autowired
    private MediaFileProcessService mediaFileProcessService;
    @Autowired
    private MediaFileService mediaFileService;
    //ffmpeg在本机的路径
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;


    /**
     * 分片广播任务
     */
    @XxlJob("VideoJobHandler")
    public void shardingJobHandler() throws Exception {
        // 1.分片参数
        //1.1执行器的序号，从0开始
        int shardIndex = XxlJobHelper.getShardIndex();
        //1.2执行器的总数
        int shardTotal = XxlJobHelper.getShardTotal();
        //确定cpu的核心数
        int processors = Runtime.getRuntime().availableProcessors();
        //2.查询待处理任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardTotal, shardIndex, processors);
        //3.获取任务数量，以创建线程池
        int size = mediaProcessList.size();
        log.debug("取到任务的数量：{}",size);
        if(size<=0){
            return ;
        }
        //4.创建一个线程池，用于执行视频转码任务
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //5.遍历任务
        //使用计数器
        CountDownLatch countDownLatch=new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            //6.将每一个任务都加入线程池
            executorService.execute(()->{
                try {
                    //执行任务逻辑
                    Long taskId = mediaProcess.getId();
                    String fileId = mediaProcess.getFileId();
                    //3.获取任务的分布式锁
                    boolean isSuccess = mediaFileProcessService.startTask(taskId);
                    //判断获取是否成功
                    if(!isSuccess){
                        //获取失败，该任务已经被执行或者超过失败次数上限,直接返回
                        return ;
                    }
                    //4.执行视频转码任务
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    //下载minio的视频到本地
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    //判断是否为空
                    if(file==null){
                        log.error("下载视频出错了,任务id:{},bucket:{},objectName:{}",taskId,bucket,objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"下载视频失败");
                        return ;
                    }
                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = fileId+".mp4";
                    //转换后mp4文件的路径
                    //创建一个临时文件
                    File tempFile = null;
                    try {
                        tempFile = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.error("创建临时文件异常:{}",e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"创建临时文件失败");
                        return ;
                    }
                    String mp4_path = tempFile.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    if(!"success".equals(result)){
                        //转码失败，保存失败详细
                        log.error("视频转码失败,任务id:{},bucket:{},objectName:{},error:{}",taskId,mediaProcess.getBucket(),mediaProcess.getFilePath(),result);
                        mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"视频转码失败");
                        return ;
                    }
                    //5.将得到的mp4视频上传到minio
                    boolean addMediaFiles2MinIo = mediaFileService.addMediaFiles2MinIo(mp4_path, "video/mp4", bucket, objectName);
                    if (!addMediaFiles2MinIo){
                        log.error("上传视频失败,任务id:{},bucket:{},objectName:{}",taskId,bucket,objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"上传视频失败");
                        return ;
                    }
                    //6.调用方法，保存任务的处理结果
                    String url = getFilePath(fileId, ".mp4");
                    mediaFileProcessService.saveProcessFinishStatus(taskId,"2",fileId,url,null);
                } finally {
                    //只要一个线程结束，计数器就减一，不管执行成功或失败
                    countDownLatch.countDown();
                }

            });
        });
        //阻塞,指定最大限度的等待时间，这里最多等待30分钟
        countDownLatch.await(30, TimeUnit.MINUTES);

    }

    /**
     * 拼接url
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }
}

