package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-18
 * @Description: es添加索引的降级逻辑
 * @Version: 1.0
 */
@Component
@Slf4j
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("远程调用增加索引错误，进入到降级逻辑，索引信息：{}，出错原因：{}",courseIndex, throwable.getMessage());
                //走降级，返回false
                return false;
            }
        };
    }
}
