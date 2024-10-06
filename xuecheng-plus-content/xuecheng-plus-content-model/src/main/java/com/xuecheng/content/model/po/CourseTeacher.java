package com.xuecheng.content.model.po;

import com.baomidou.mybatisplus.annotation.*;
import com.xuecheng.base.exception.ValidationGroups;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 课程-教师关系表
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("course_teacher")
public class CourseTeacher implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程标识
     */
    //Long判断不能为空，要用NotNull，不然会报错
    @NotNull(message = "课程id不能为空" ,groups = ValidationGroups.Insert.class)
    private Long courseId;

    /**
     * 教师标识
     */
    @NotEmpty(message = "教师标识不能为空",groups = ValidationGroups.Insert.class)
    private String teacherName;

    /**
     * 教师职位
     */
    @NotEmpty(message = "教师职位不能为空",groups = ValidationGroups.Insert.class)
    private String position;

    /**
     * 教师简介
     */
    private String introduction;

    /**
     * 照片
     */
    private String photograph;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

}
