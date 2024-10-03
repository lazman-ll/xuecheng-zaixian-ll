package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 课程基本信息测试类
 */
@SpringBootTest
public class CourseCategoryServiceTests {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    void testCategoryService() {
        List<CourseCategoryTreeDto> categoryTreeDtoList = courseCategoryService.queryTreeNode("1");
        System.out.println("categoryTreeDtoList = " + categoryTreeDtoList);
    }
}
