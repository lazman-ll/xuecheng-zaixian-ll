package com.xuecheng.learning.service;

import org.springframework.amqp.core.Message;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-11-02
 * @Description: 接受消息并处理接口
 * @Version: 1.0
 */
public interface ReceivePayNotifyService {

    /**
     * 接受消息并处理
     * @param message
     */
    public void receivePayNotify(Message message);


}
