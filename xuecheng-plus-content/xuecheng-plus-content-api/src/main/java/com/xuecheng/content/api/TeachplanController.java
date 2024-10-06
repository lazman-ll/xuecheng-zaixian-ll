package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 课程计划新增或修改
     * @param saveTeachplanDto 新课程计划信息
     */
    @PostMapping("/teachplan")
    @ApiOperation("课程计划新增或修改")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto){
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    /**
     * 课程计划的删除
     * @param id 课程计划id
     */
    @DeleteMapping("/teachplan/{id}")
    @ApiOperation("删除课程计划")
    public void deleteTeachplan(@PathVariable("id") Long id){
        teachplanService.deleteTeachplan(id);
    }

    /**
     * 课程计划排序
     * @param moveType
     * @param id
     */
    @PostMapping("/teachplan/{moveType}/{id}")
    @ApiOperation("课程计划排序")
    public void moveTeachplan(@PathVariable("moveType") String moveType,@PathVariable("id") Long id){
        teachplanService.moveTeachplan(moveType,id);
    }
}
