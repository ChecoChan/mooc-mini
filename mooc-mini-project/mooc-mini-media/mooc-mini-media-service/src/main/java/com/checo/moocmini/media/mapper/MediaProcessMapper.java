package com.checo.moocmini.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.checo.moocmini.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Checo
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /**
     * 获取待处理任务
     *
     * @param shardTotal 分片总数
     * @param shardIndex 分片序号
     * @param count      获取记录数
     */
    @Select("select * from media_process mp where mp.id % #{shardTotal} = #{shardIndex} and (mp.status = 1 or mp.status = 3) and mp.fail_count < 3 limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);

    /**
     * 开启一个任务
     *
     * @param id 任务id
     * @return 更新记录数
     */
    @Update("update media_process mp set mp.status= '4' where (mp.status='1' or mp.status='3') and mp.fail_count < 3 and mp.id = #{id}")
    int startTask(@Param("id") long id);
}
