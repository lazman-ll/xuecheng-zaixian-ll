package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-17
 * @Description: 远程调用文件上传的降级逻辑
 * @Version: 1.0
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {

    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile filedata, String objectName) {
                log.debug("远程调用文件上传服务失败:{}",throwable.getMessage());
                return null;
            }
        };
    }
}
