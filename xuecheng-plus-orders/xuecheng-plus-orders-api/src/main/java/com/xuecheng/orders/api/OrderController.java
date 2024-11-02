package com.xuecheng.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-31
 * @Description: 订单接口
 * @Version: 1.0
 */
@Controller
@Api(tags = "订单接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    /**
     * 生成支付二维码
     * @param addOrderDto
     * @return
     */
    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {
        //获取下单用户信息
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user==null){
            XueChengPlusException.cast("请先登录");
        }
        String userId = user.getId();
        //调用service接口生成支付二维码
        return orderService.createOrder(userId ,addOrderDto);

    }

    /**
     * 扫码下单接口
     * @param payNo
     * @param httpResponse
     * @throws IOException
     */
    @ApiOperation("扫码下单接口")
    @GetMapping("/requestpay")
    public void requestpay(String payNo, HttpServletResponse httpResponse) throws IOException {
        //请求支付宝下单
        //通过传来的支付号，判断支付记录是否存在
        XcPayRecord payRecordByPayno = orderService.getPayRecordByPayno(payNo);
        if(payRecordByPayno==null){
            XueChengPlusException.cast("支付记录不存在");
        }
        //获取支付状态
        String status = payRecordByPayno.getStatus();
        if("601002".equals(status)){
            XueChengPlusException.cast("已支付，无需重复支付");
        }
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        alipayRequest.setNotifyUrl("http://5ed7696c.r8.cpolar.top/orders/receivenotify");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\""+payNo+"\"," +
                "    \"total_amount\":"+payRecordByPayno.getTotalPrice()+"," +
                "    \"subject\":\""+payRecordByPayno.getOrderName()+"\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }");//填充业务参数


        String form = null; //调用SDK生成表单
        try {
            form = alipayClient.pageExecute(alipayRequest,"POST").getBody();
        } catch (AlipayApiException e) {
            XueChengPlusException.cast("下单出错，请重试");
        }
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();
    }

    /**
     * 查询支付结果,并根据支付状态更新相关表
     * @param payNo
     * @return
     * @throws IOException
     */
    @ApiOperation("查询支付结果")
    @GetMapping("/payresult")
    @ResponseBody
    public PayRecordDto payresult(String payNo) throws IOException {
        return orderService.queryPayResult(payNo);
    }

    /**
     *
     * @param request
     * @param response
     * @throws UnsupportedEncodingException
     * @throws AlipayApiException
     */
    @ApiOperation("接收支付结果通知")
    @PostMapping("/receivenotify")
    public void receivenotify
    (HttpServletRequest request, HttpServletResponse response)
            throws IOException, AlipayApiException{
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if(verify_result){//验证成功
            //商户交易号
            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号
            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
            //交易金额
            String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"),"UTF-8");
            //——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
            if (trade_status.equals("TRADE_SUCCESS")){
                PayStatusDto payStatusDto = new PayStatusDto();
                payStatusDto.setTrade_status(trade_status);
                payStatusDto.setOut_trade_no(out_trade_no);
                payStatusDto.setApp_id(APP_ID);
                payStatusDto.setTotal_amount(total_amount);
                payStatusDto.setTrade_no(trade_no);
                orderService.saveAliPayStatus(payStatusDto);
            }
            response.getWriter().write("success");
        }else{//验证失败
            response.getWriter().write("fail");
        }
    }

}
