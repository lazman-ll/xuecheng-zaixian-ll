package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 课程基本信息测试类
 */
@SpringBootTest
public class TeachplanMapperTests {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Test
    void testTeachPlanMapper() {
        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println("teachPlanDtos = " + teachPlanDtos);
    }
}
