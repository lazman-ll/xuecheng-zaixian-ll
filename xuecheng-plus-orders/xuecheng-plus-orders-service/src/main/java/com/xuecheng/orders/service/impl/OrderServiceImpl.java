package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.JsonUtil;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Var;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-31
 * @Description: 订单相关接口实现
 * @Version: 1.0
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private XcOrdersMapper ordersMapper;
    @Autowired
    private XcOrdersGoodsMapper ordersGoodsMapper;
    @Autowired
    private XcPayRecordMapper payRecordMapper;
    @Autowired
    private OrderServiceImpl currentProxy;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MqMessageService mqMessageService;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;
    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        //向订单表中添加订单数据
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);

        //保存支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        Long payNo = payRecord.getPayNo();

        //调用工具生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        //支付二维码的url
        //TODO 更换隧道
        String url = String.format(qrcodeurl, payNo);
        //用于保存生成的二维码信息
        String qrCode = null;
        try {
             qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.cast("生成二维码出错");
        }
        //封装信息，并返回
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }


    /**
     * 插入订单表
     * @param userId
     * @param addOrderDto
     * @return
     */
    private XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto){
        //判断是否已经生成过该订单
        XcOrders xcOrders = getOrderByOutBusinessId(addOrderDto.getOutBusinessId());
        if(xcOrders!=null){
            return xcOrders;
        }
        //向订单主表插入数据
        xcOrders =new XcOrders();
        //是一名雪花算法生成订单id（保证唯一）
        xcOrders.setId(IdWorkerUtils.getInstance().nextId());
        //订单金额
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        //订单创建时间
        xcOrders.setCreateDate(LocalDateTime.now());
        //订单状态，默认为未支付
        xcOrders.setStatus("600001");
        //创建订单的用户
        xcOrders.setUserId(userId);
        //订单类型
        xcOrders.setOrderType("60201");
        //订单名称
        xcOrders.setOrderName(addOrderDto.getOrderName());
        //订单描述
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        //订单明细
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        //外部业务号，例如选课表的主键id
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());
        int insert = ordersMapper.insert(xcOrders);
        if(insert<=0){
            XueChengPlusException.cast("创建订单失败");
        }
        //向订单明细表中插入数据
        //将前端传来的json转为List
        List<XcOrdersGoods> xcOrdersGoodss = JsonUtil.jsonToList(addOrderDto.getOrderDetail(), XcOrdersGoods.class);
        if(xcOrdersGoodss==null || xcOrdersGoodss.size()<=0){
            XueChengPlusException.cast("添加订单明细为空");
        }
        for (XcOrdersGoods goods : xcOrdersGoodss) {
            //设置订单id
            goods.setOrderId(xcOrders.getId());
            int insert1 = ordersGoodsMapper.insert(goods);
            if(insert1<=0){
                XueChengPlusException.cast("添加订单明细失败");
            }
        }
        return xcOrders;
    }

    /**
     * 创建支付记录
     * @param xcOrders
     * @return
     */
    private XcPayRecord createPayRecord(XcOrders xcOrders){
        //获取订单id
        Long ordersId = xcOrders.getId();
        xcOrders = ordersMapper.selectById(ordersId);
        //如果此订单不存在，不能添加支付记录
        if(xcOrders==null){
            XueChengPlusException.cast("订单不存在");
        }
        //订单状态
        String status = xcOrders.getStatus();
        if("600002".equals(status)){
            //此订单已支付，无需在支付
            XueChengPlusException.cast("此订单已支付");
        }
        //添加支付记录
        XcPayRecord xcPayRecord = new XcPayRecord();
        //本系统支付交易号（要传给支付宝）
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());
        //商品订单号
         xcPayRecord.setOrderId(ordersId);
         //商品名称
        xcPayRecord.setOrderName(xcOrders.getOrderName());
        //支付金额
        xcPayRecord.setTotalPrice(xcOrders.getTotalPrice());
        //币种
        xcPayRecord.setCurrency("CNY");
        //创建时间
        xcPayRecord.setCreateDate(LocalDateTime.now());
        //支付状态，未支付
        xcPayRecord.setStatus("601001");
        //用户id
        xcPayRecord.setUserId(xcOrders.getUserId());
        int insert = payRecordMapper.insert(xcPayRecord);
        if(insert<=0){
            XueChengPlusException.cast("添加支付记录失败");
        }
        return xcPayRecord;
    }

    /**
     * 根据业务id查询订单，业务id为选课记录表中的主键
     * @param outBusinessId
     * @return
     */
    private XcOrders getOrderByOutBusinessId(String outBusinessId) {
        LambdaQueryWrapper<XcOrders> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(XcOrders::getOutBusinessId,outBusinessId);
        return ordersMapper.selectOne(queryWrapper);
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        return  payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {
        //调用支付宝接口查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //根据支付结果，更新支付记录表以及订单表
        currentProxy.saveAliPayStatus(payStatusDto);
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecordByPayno,payRecordDto);
        return payRecordDto;
    }

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付交易号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo){
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            XueChengPlusException.cast("支付状态查询失败");
        }
        if(!response.isSuccess()){
            XueChengPlusException.cast("交易失败");
        }
        String resultJson = response.getBody();
        //转map
        Map bodyMap = JSON.parseObject(resultJson, Map.class);
        Map<String,String> resultMap = (Map<String,String>) bodyMap.get("alipay_trade_query_response");
        //解析支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_no(resultMap.get("trade_no"));
        payStatusDto.setTrade_status(resultMap.get("trade_status"));
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount(resultMap.get("total_amount"));
        return payStatusDto;
    }

    /**
     * @description 保存支付宝支付结果
     * @param payStatusDto  支付结果信息
     * @return void
     */
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto){
        //根据支付结果，更新支付记录表以及订单表
        //获取支付记录号
        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
        if(payRecordByPayno==null){
            XueChengPlusException.cast("支付记录不存在");
        }
        //订单id
        Long orderId = payRecordByPayno.getOrderId();
        XcOrders xcOrders = ordersMapper.selectById(orderId);
        if(xcOrders==null){
            XueChengPlusException.cast("相关联的订单不存在");
        }
        //支付状态
        String status = payRecordByPayno.getStatus();
        if("601002".equals(status)){
            //已支付，无需重复支付
            return;
        }
        //判断是否支付成功
        String tradeStatus = payStatusDto.getTrade_status();
        if(!"TRADE_SUCCESS".equals(tradeStatus)){
            //支付失败，直接结束
            return;
        }
        //更新支付记录表
        payRecordByPayno.setStatus("601002");
        payRecordByPayno.setPaySuccessTime(LocalDateTime.now());
        payRecordByPayno.setOutPayNo(payStatusDto.getTrade_no());
        payRecordByPayno.setOutPayChannel("AliPay");
        int update1 = payRecordMapper.updateById(payRecordByPayno);
        if(update1<=0){
            XueChengPlusException.cast("更新支付记录失败");
        }
        //更新订单表
        xcOrders.setStatus("600002");
        int update2 = ordersMapper.updateById(xcOrders);
        if(update2<=0){
            XueChengPlusException.cast("更新订单失败");
        }
        //发送消息，并将消息持久化
        //消息持久化
        MqMessage mqMessage = mqMessageService.addMessage
                ("payresult_notify",
                        xcOrders.getOutBusinessId(),
                        xcOrders.getOrderType(), null);
        //发送消息
        notifyPayResult(mqMessage);

    }

    @Override
    public void notifyPayResult(MqMessage message) {
        //消息本身的内容
        String jsonString = JSON.toJSONString(message);
        //创建一个持久化消息
        Message buildMessage = MessageBuilder
                .withBody(jsonString.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        //消息的id（要保证全局唯一性）
        Long id = message.getId();
        //可以指定回调方法
        CorrelationData correlationData = new CorrelationData(id.toString());
        //ConfirmCallback()保证消息到交换机
        correlationData.getFuture().addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable throwable) {
                //mq内部发生错误，一般不会出现
                log.error("消息:{}，发送异常：{}",jsonString,throwable.getMessage());
            }

            @Override
            public void onSuccess(CorrelationData.Confirm confirm) {
                if(confirm.isAck()){
                    //消息发送成功
                    log.info("消息发送成功:{}，消息id：{}",jsonString,id);
                    //将消息从数据库表中删除
                    mqMessageService.completed(id);
                }else {
                    //消息发送失败
                    log.info("消息发送失败:{}，消息id：{}",jsonString,id);
                }
            }
        });
        //发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", buildMessage, correlationData);
    }
}
