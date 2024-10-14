package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /**
     * @author 闲人指路
     * @description 根据分片参数查询待处理任务
     * @dateTime 20:05 2024/10/11
     * @param shardIndex 当前分片序号
     * @param shardTotal 总共分片数
     * @param count 需要获取的任务数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
    */
    @Select("select * from media_process t where t.id % #{shardTotal} = #{shardIndex} and (t.status = '1' or t.status = '3') and t.fail_count < 3 limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal,@Param("shardIndex") int shardIndex,@Param("count") int count);


    /**
     * 获取任务锁(利用数据库的乐观锁实现分布式锁)
     * @author 闲人指路
     * @param id
     * @return
     */
    @Update("UPDATE media_process SET status='4' where (status = 1 or status = 3) and fail_count < 3 and id = #{id}")
    int startTask(@Param("id") Long id);
}
