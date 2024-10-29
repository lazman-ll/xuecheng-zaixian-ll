package com.xuecheng.checkcode.service.impl;



import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sun.deploy.net.HttpUtils;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import com.xuecheng.checkcode.service.AbstractCheckCodeService;
import com.xuecheng.checkcode.service.CheckCodeService;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-25
 * @Description: 手机和邮箱验证码生成器
 * @Version: 1.0
 */
@Service("NumCheckCodeService")
public class NumCheckCodeServiceImpl extends AbstractCheckCodeService implements CheckCodeService {


    @Resource(name="NumberLetterCheckCodeGenerator")
    @Override
    public void setCheckCodeGenerator(CheckCodeGenerator checkCodeGenerator) {
        this.checkCodeGenerator = checkCodeGenerator;
    }

    @Resource(name="UUIDKeyGenerator")
    @Override
    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }


    @Resource(name="RedisCheckCodeStore")
    @Override
    public void setCheckCodeStore(CheckCodeStore checkCodeStore) {
        this.checkCodeStore = checkCodeStore;
    }


    @Override
    public CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto) {
        //生成验证码
        GenerateResult generate = generate(checkCodeParamsDto, 4, "checkcode:", 300);
        String key = generate.getKey();
        String code = generate.getCode();
        CheckCodeResultDto checkCodeResultDto = new CheckCodeResultDto();
        checkCodeResultDto.setAliasing(null);
        checkCodeResultDto.setKey(key);

        //判断是手机号还是邮箱
        String param = checkCodeParamsDto.getParam1();
        if(param.contains("@")){
            //是邮箱,验证邮箱格式是否正确
            String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
            if(!param.matches(regex)){
                throw new XueChengPlusException("邮箱格式不正确");
            }
        }else {
            //是手机，验证手机格式是否正确
            /*String regex = "^(?:(?:\\\\+|00)86)?1[3-9]\\\\d{9}$";
            if(!param.matches(regex)){
                throw new XueChengPlusException("手机格式不正确");
            }*/
            if(!sendPhoneCode(code,param)){
                throw new XueChengPlusException("发送短信验证码失败");
            }
        }
        return checkCodeResultDto;
    }

    /**
     * 发送短信验证码
     * @param code
     * @return
     */
    private boolean sendPhoneCode(String code,String mobile){
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String appcode = "e25e621b58e44ec3991983e2fe24931a";
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        Map<String, Object> querys = new HashMap<String, Object>();
        querys.put("mobile", mobile);
        querys.put("param", "**code**:"+code+",**minute**:5");

//smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html

        querys.put("smsSignId", "4b98331f75774d81a1db790c58d591f2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
//        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从\r\n\t    \t* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java\r\n\t    \t* 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            String response = HttpRequest.post(host + path)
                    .header("Authorization", "APPCODE " + appcode)
                    .form(querys)//表单内容
                    .timeout(20000)//超时，毫秒
                    .execute().body();
            if(response.contains("成功")){
                return true;
            }else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
