package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNode(String id) {
        //1.递归查询分类信息
        List<CourseCategoryTreeDto> categoryTreeDtoList = courseCategoryMapper.childrenTreeNodes(id);
        //2.找到每个节点的子节点进行数据的封装
        //2.1将list转为map方便后面根据parentId查询父节点
        //filter(item->!id.equals(item.getId()))用于过滤根节点
        Map<String, CourseCategoryTreeDto> categoryTreeDtoMap =
                categoryTreeDtoList.stream().filter(item->!id.equals(item.getId()))
                        .collect(Collectors.toMap(key -> key.getId(),
                                value -> value, (key1, key2) -> key2));
        //2.2找到每个节点的父节点，并放入父节点的childrenTreeNodes中
        List<CourseCategoryTreeDto> resultList=new ArrayList<>();
        categoryTreeDtoList.stream().forEach(item->{
            //2.2.1判断该节点是否时根节点的子节点
            if(id.equals(item.getParentid())){
                //是，放入resultList中
                resultList.add(item);
            }else{
                //2.2.2不是，找到该节点的父节点
                CourseCategoryTreeDto categoryTreeParent = categoryTreeDtoMap.get(item.getParentid());
                //2.2.2.1判断其父节点是否为null，预防空指针
                if(categoryTreeParent!=null){
                    //2.2.2.2判断其父节点的CourseCategoryTreeDto是否为空
                    if(categoryTreeParent.getChildrenTreeNodes()==null){
                        //2.2.2.3为空，new一个并为其复制
                        categoryTreeParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                    }
                    //2.2.2.4将其添加到父节点的CourseCategoryTreeDto中
                    categoryTreeParent.getChildrenTreeNodes().add(item);
                }
            }
        });
        return resultList;
    }
}
