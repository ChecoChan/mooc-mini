package com.checo.moocmini.search.service;

import com.checo.moocmini.base.model.PageParams;
import com.checo.moocmini.search.dto.SearchCourseParamDto;
import com.checo.moocmini.search.dto.SearchPageResultDto;
import com.checo.moocmini.search.po.CourseIndex;

/**
 * 课程搜索service
 */
public interface CourseSearchService {

    /**
     * 搜索课程列表
     *
     * @param pageParams           分页参数
     * @param searchCourseParamDto 搜索条件
     * @return com.checo.moocmini.base.model.PageResult<com.checo.moocmini.search.po.CourseIndex> 课程列表
     */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);
}
