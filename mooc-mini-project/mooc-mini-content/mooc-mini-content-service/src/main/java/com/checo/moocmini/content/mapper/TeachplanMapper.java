package com.checo.moocmini.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.checo.moocmini.content.model.dto.TeachplanDto;
import com.checo.moocmini.content.model.po.Teachplan;

import java.util.List;


/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author Checo
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    // 使用递归查询课程计划
    List<TeachplanDto> selectTreeNodes(Long courseId);
}
