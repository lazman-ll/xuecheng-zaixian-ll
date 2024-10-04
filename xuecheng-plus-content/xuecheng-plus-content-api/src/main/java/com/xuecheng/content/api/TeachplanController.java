package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api("课程计划编辑接口")
@Slf4j
public class TeachplanController {


    @Autowired
    private TeachplanService teachplanService;

    /**
     * 查询课程计划树形结构
     * @param courseId 课程id
     * @return 返回的课程计划
     */
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    @ApiOperation("查询课程计划树形结构")
    public List<TeachPlanDto> getTreeNodes(@PathVariable("courseId") Long courseId){
        return teachplanService.findTeachplanTree(courseId);
    }
}
