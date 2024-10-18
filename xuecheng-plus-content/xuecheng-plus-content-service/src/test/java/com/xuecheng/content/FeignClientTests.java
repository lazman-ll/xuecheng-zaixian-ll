package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-17
 * @Description: 测试远程调用服务
 * @Version: 1.0
 */
@SpringBootTest
public class FeignClientTests {


    @Autowired
    private MediaServiceClient mediaServiceClient;
    @Test
    public void testFeignClient(){
        File file = new File("D:\\MyFile\\22.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String upload = mediaServiceClient.upload(multipartFile, "course/22.html");
        System.out.println("upload = " + upload);
    }
}
