package com.checo.moocmini.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.checo.moocmini.content.model.dto.CourseCategoryTreeDto;
import com.checo.moocmini.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author Checo
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    // 使用递归查询分类
    List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
