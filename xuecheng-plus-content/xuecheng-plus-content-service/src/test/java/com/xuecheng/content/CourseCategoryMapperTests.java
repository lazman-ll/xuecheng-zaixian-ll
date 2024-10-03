package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 课程基本信息测试类
 */
@SpringBootTest
public class CourseCategoryMapperTests {

    @Autowired
    private CourseCategoryMapper categoryMapper;

    @Test
    void testBaseMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = categoryMapper.childrenTreeNodes("1");
        System.out.println("courseCategoryTreeDtos = " + courseCategoryTreeDtos);
    }
}
