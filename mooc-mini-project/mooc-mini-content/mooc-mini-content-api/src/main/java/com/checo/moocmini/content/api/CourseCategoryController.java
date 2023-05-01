package com.checo.moocmini.content.api;

import com.checo.moocmini.content.model.dto.CourseCategoryTreeDto;
import com.checo.moocmini.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "课程分类接口", tags = "课程分类接口")
@RestController
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @ApiOperation("查询课程分类树形结构")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryCourseCategoryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }
}
