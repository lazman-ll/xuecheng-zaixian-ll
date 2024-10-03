package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;

    /**
     * 查询课程分类接口
     * @return
     */
    @GetMapping("course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNode(){
        List<CourseCategoryTreeDto> categoryTreeDtoList =courseCategoryService.queryTreeNode("1");
        return categoryTreeDtoList;
    }
}
