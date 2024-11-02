package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-31
 * @Description: 订单相关service接口
 * @Version: 1.0
 */
public interface OrderService {

    /**
     * 生成支付二维码
     * @param userId
     * @param addOrderDto
     * @return
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * @description 查询支付记录
     * @param payNo  交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     */
    XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 查询支付结果
     * @param payNo
     * @return
     */
    PayRecordDto queryPayResult(String payNo);

    /**
     * 保存支付结果
     * @param payStatusDto
     */
    public void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 发送通知结果
     * @param message
     */
    public void notifyPayResult(MqMessage message);
}
