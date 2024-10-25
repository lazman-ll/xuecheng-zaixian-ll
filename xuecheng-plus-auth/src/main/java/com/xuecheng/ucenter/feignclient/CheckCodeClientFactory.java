package com.xuecheng.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-20
 * @Description: 远程调用验证校验接口的降级逻辑
 * @Version: 1.0
 */
@Slf4j
@Component
public class CheckCodeClientFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.error("远程调用验证码服务失败:{}",throwable.getMessage());
                return null;
            }
        };
    }
}
