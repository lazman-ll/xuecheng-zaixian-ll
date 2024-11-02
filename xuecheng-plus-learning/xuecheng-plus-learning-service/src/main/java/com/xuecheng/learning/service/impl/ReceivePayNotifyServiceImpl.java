package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.MyCourseTableService;
import com.xuecheng.learning.service.ReceivePayNotifyService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyStore;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-11-02
 * @Description: 接受rabbitmq的消息通知
 * @Version: 1.0
 */
@Service
@Slf4j
public class ReceivePayNotifyServiceImpl implements ReceivePayNotifyService {

    @Autowired
    private MyCourseTableService myCourseTableService;

    @Override
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receivePayNotify(Message message) {

        //重试前先休眠5秒，不要连着重试
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //解析出消息
        byte[] body = message.getBody();
        String jsonString = new String(body);
        //转为对象
        MqMessage mqMessage = JSON.parseObject(jsonString, MqMessage.class);
        //根据消息的内容，向数据库插入内容
        //选课id
        String chooseCourseId = mqMessage.getBusinessKey1();
        //订单类型
        String orderType = mqMessage.getBusinessKey2();
        if("60201".equals(orderType)){
            boolean isSuccess = myCourseTableService.saveChooseCourseSuccess(chooseCourseId);
            if(!isSuccess){
                log.error("保存选课记录状态失败，选课id：{}",chooseCourseId);
                XueChengPlusException.cast("保存选课记录状态失败");
            }
        }
    }


}
