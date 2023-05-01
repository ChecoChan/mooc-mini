package com.checo.moocmini.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.checo.moocmini.content.model.dto.CourseCategoryTreeDto;
import com.checo.moocmini.content.model.po.CourseCategory;

import java.util.List;

public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
