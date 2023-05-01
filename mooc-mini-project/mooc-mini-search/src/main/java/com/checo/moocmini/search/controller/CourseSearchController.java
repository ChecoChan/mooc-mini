package com.checo.moocmini.search.controller;

import com.checo.moocmini.base.model.PageParams;
import com.checo.moocmini.search.dto.SearchCourseParamDto;
import com.checo.moocmini.search.dto.SearchPageResultDto;
import com.checo.moocmini.search.po.CourseIndex;
import com.checo.moocmini.search.service.CourseSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 课程搜索接口
 */
@Api(value = "课程搜索接口", tags = "课程搜索接口")
@RestController
@RequestMapping("/course")
public class CourseSearchController {

    @Autowired
    private CourseSearchService courseSearchService;


    @ApiOperation("课程搜索列表")
    @GetMapping("/list")
    public SearchPageResultDto<CourseIndex> list(PageParams pageParams, SearchCourseParamDto searchCourseParamDto) {

        return courseSearchService.queryCoursePubIndex(pageParams, searchCourseParamDto);

    }
}
