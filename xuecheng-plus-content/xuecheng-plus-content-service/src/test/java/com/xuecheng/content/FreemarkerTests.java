package com.xuecheng.content;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * @Author: 闲人指路
 * @CreateTime: 2024-10-17
 * @Description: Freemarker测试
 * @Version: 1.0
 */
@SpringBootTest
public class FreemarkerTests {

    @Autowired
    private CoursePublishService coursePublishService;

    /**
     * 测试freemarker的页面静态化
     * @throws Exception
     */
    @Test
    public void testGenerateHtmlByTemplate() throws Exception{
        Configuration configuration = new Configuration(Configuration.getVersion());
        //获取classpath路径
        String classpath = this.getClass().getResource("/").getPath();
        //指定目录
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        //指定编码
        configuration.setDefaultEncoding("utf-8");
        //获取模板
        Template template = configuration.getTemplate("course_template.ftl");
        //查询课程的信息，作为模型数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(22L);
        HashMap<String, Object> modelMap = new HashMap<>();
        //设置模型数据
        modelMap.put("model", coursePreviewInfo);
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, modelMap);
        //使用流将html写入文件
        //输入流
        InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
        //输出流
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\MyFile\\22.html"));
        //使用流将html写入文件
        IOUtils.copy(inputStream, outputStream);
    }
}
