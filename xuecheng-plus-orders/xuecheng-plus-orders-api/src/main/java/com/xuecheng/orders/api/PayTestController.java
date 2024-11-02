package com.xuecheng.orders.api;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.xuecheng.orders.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-30
 * @Description: 扫码支付测试接口
 * @Version: 1.0
 */
@Controller
public class PayTestController {
    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;



    @RequestMapping("/alipaytest")
    public void doPost(HttpServletRequest httpRequest,
                       HttpServletResponse httpResponse) throws ServletException, IOException, AlipayApiException {
        //获得初始化的AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
//        alipayRequest.setReturnUrl("http://domain.com/CallBack/return_url.jsp");
        alipayRequest.setNotifyUrl("http://f98a4df.r8.cpolar.top/orders/paynotify");//在公共参数中设置回跳和通知地址
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"20240320010101017\"," +
                "    \"total_amount\":0.1," +
                "    \"subject\":\"Iphone16 16G\"," +
                "    \"product_code\":\"QUICK_WAP_WAY\"" +
                "  }");//填充业务参数

        /*AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo("70501111111S001111119");
        model.setTotalAmount("9.00");
        model.setSubject("大乐透");
        model.setProductCode("QUICK_WAP_WAY");
        model.setSellerId("2088102147948060");
        alipayRequest.setBizModel(model);*/

        String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        httpResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
        httpResponse.getWriter().write(form);//直接将完整的表单html输出到页面
        httpResponse.getWriter().flush();


        /*String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCJ98scmfTdmsMTX0GBlSl8IwXZVmbGf1ZG3DPg3DZTgkTR4xBylaUf1ugR/iyvfou4BQMGvmceBJ6Ctg68QEQcw2iXOOh3eQqi2StxkgUdqVRXp5Vi75cTT11AWP9lozhcEdsqInkK9zjz8wMxhm/nW+O0S/c6QlGnVSj7lGA4miP0cu8WaUFWkAZOnCatp4XV9uKo0GNQ5N6baWWDvzUKYt0esTqekP6A/Q4iLGTiWWqqDgHaUAJqBnsML3nYYtx3Ro+1qrgu+3f9BDpZnBILvYHWX+jGTjuiVimceMo8YawM54KUiA5TsM6ugu8ik3rBrzgSCGhJRuiq/yrYkxOBAgMBAAECggEAe+9K1kN6UgLuz50W5AeQ/4EahPR3WEIUx4yRUaGfb36qwXveT4Abl7FeQKN5kp/zdHeUE1/Ak5eEqFhQfkygotHqR0WIlaH2qvi4bMnFrLJqIF9a80fUBzWYx+/qZGuteg9UN9JFt1MDJyiaKsfgCA1kS5ad3Ais/sqiCPRtRwnlrVIvVIYdBkSIJRF8BkdfCl89/CFs/ryKm8ZmPew4GxTRUk8QMkkjohnLU4GEv5p/8Z177WXbi+5UHZ2+yumFgIhfgE0NxOUfKq3NXQCdkplSvR2/V6lbCA2yKDG56+xWZNIKUtr24Q0Ovxc1pNjM3PF1N3xpbaSIXaghOoNhAQKBgQDGsdIDEIiJ3sI8ywAXKzaZaM0FbsB51wTOV3/PfedX4aQZeIndycfmvK74U2y3NnM141PCG6UoXXUBx6LSLqrhpRp1wmwQViA9lOK38Z1EnPbg5A1opt+M4fl3xG2F9M8dXuMO2yEuNVpNEHCsP1SyMvir1Msdbp2kU5NG3Gi3EQKBgQCxwlpi+Qw+IEQtaMxl45SP+E0L10wlkk6dHIzEdX45V20ypwR2urKM9SU5k7W/6+vGMZOoe6mVDXFeXAuAw0pwgiA0KPI57Rlb7zrdggncztxQKsbP2b/FMBbL0EcydgNj/olYfFkJDh0tO/7j0P4hnrooB0stcNLNv6X02rf1cQKBgQC/FbYmElltVhixBH2CyqFIqfw6FEfxJPNPSeD1+FxrLOp3QHA+JtcjtSQEV56/GBN2qLgdfh64kiB75LVWs2DNfs3SoHbywxFgh/X99tRLbzUSFOA8W/ez8oLLRtIZuNUSpvVwjRb87Hx6jqv9Sy617cUu44evN3lN1r5VsJTYMQKBgEo2awgBE/RoAi63Hwa2wdJthrUX2Kmi8IE1ZyuqJd2Iu4U3J2zuOxcQTc9A9NNAirm+/veLUVxyNU+AvCiw38Yi/ZDpO1+ltj2knf3WiYmPL3mkskYaka6gihPjS6YskPPRU8QGfiGCq/mzn5G7Mi4VF4EYsnExNkFB7uZP3hdxAoGBAINt/qD7XdlrU8xovXbn5Qww18q0a2001nR7BLeGLD5mKVFfx8jaoWJ+ASIHAhRfYAHbvbQ2u0LwFgGw4xmilkan5zuYXtYD55fEQ0U15fa9kv28b9HsT8oABEobrMIU0jVPE4Mj/GzbssUcCW1cuq6p1rnfutmXJzQkCw1cvRlg";
        String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwUEqFJkWZFbRyLDtl4lD102WiUhQVXONOC7zeWL3Ic1sMLSpDxy2g1NA2aepmdsPY/TVrKZWrr0TGRnIUNA8gPm/WL5TN8UQKZu2BsrCbNHz7UoYDwt3QYaeyzLtQMN82igt45p0HgezHPR7STBWkmpHBd2MWSwvMP3sjZpOGPSm14PXKaHj2RpjJncz5tTzW49fQwks+e6vkkQQQCUcED7Rtcht1YFopIG2W8JZdXhSYAeLlW0bffy188PYdAR2w0MbelNfMGIcslO/5Rz44oI59dCq+sC9O6nzIy+rLI7f4dwfutCNh6nXXGtkO01k3iqweqijeSpaJCS8KB4tzwIDAQAB";
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setServerUrl("https://openapi-sandbox.dl.alipaydev.com/gateway.do");
        alipayConfig.setAppId("9021000141666644");
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setFormat("json");
        alipayConfig.setAlipayPublicKey(alipayPublicKey);
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);*/

//        AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "POST");
        // 如果需要返回GET请求，请使用
        // AlipayTradeWapPayResponse response = alipayClient.pageExecute(request, "GET");
       /* String pageRedirectionData = response.getBody();
        System.out.println(pageRedirectionData);
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
            // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            // System.out.println(diagnosisUrl);
        }*/
    }

    @PostMapping("/paynotify")
    public void paynotify(HttpServletRequest request,
                           HttpServletResponse response) throws IOException, AlipayApiException {
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
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
        //商户订单号


        //获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
        //计算得出通知验证结果
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean verify_result = AlipaySignature.rsaCheckV1(params, ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, "RSA2");

        if(verify_result){//验证成功
            //////////////////////////////////////////////////////////////////////////////////////////
            //请在这里加上商户的业务逻辑程序代码

            String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
            //支付宝交易号

            String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

            //交易状态
            String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");


            //——请根据您的业务逻辑来编写程序（以下代码仅作参考）——

            if(trade_status.equals("TRADE_FINISHED")){
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                //如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            } else if (trade_status.equals("TRADE_SUCCESS")){
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //如果签约的是可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
            }

            //——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
            //验证成功
            response.getWriter().write("success");

            //////////////////////////////////////////////////////////////////////////////////////////
        }else{//验证失败
            response.getWriter().write("fail");
        }

    }
}
