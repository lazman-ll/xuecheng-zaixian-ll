package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {
    /**
     * 查询分类
     * @param id
     * @return
     */
    List<CourseCategoryTreeDto> queryTreeNode(String id);
}
