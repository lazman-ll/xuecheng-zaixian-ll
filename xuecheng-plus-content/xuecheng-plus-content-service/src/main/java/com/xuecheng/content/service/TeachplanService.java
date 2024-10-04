package com.xuecheng.content.service;

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
}
