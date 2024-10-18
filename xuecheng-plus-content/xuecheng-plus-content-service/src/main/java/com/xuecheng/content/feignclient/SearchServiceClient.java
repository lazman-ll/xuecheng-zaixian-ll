package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-18
 * @Description: 远程调用es中的添加索引
 * @Version: 1.0
 */
@FeignClient(value = "search",fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface SearchServiceClient {


    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
