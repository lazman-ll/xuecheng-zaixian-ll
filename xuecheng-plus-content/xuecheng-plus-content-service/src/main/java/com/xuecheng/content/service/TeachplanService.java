package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

/**
 * 课程计划管理service
 */
public interface TeachplanService {

    /**
     * 查询课程计划
     * @param courseId 课程id
     * @return
     */
    public List<TeachPlanDto> findTeachplanTree(Long courseId);

    /**
     * 课程计划新增或修改
     * @param saveTeachplanDto 新课程计划信息
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 课程计划删除
     * @param id 课程计划id
     */
    void deleteTeachplan(Long id);

    /**
     * 课程计划排序
     * @param moveType
     * @param id
     */
    void moveTeachplan(String moveType, Long id);
}
