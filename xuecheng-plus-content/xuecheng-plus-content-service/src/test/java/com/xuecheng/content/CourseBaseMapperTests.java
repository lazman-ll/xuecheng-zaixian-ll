

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
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
public class CourseBaseMapperTests {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Test
    void testBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(18L);
        //断言其不为空
        Assertions.assertNotNull(courseBase);

        //查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CourseBase::getName,"java");
        //分页查询
        Page<CourseBase> page =new Page(1L,5L);
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> records = courseBasePage.getRecords();
        PageResult<CourseBase> pageResult = new PageResult<>(records,courseBasePage.getTotal(),1L,5L);

    }
}
