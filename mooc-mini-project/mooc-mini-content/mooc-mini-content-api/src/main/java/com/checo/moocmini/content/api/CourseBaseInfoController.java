package com.checo.moocmini.content.api;

import com.checo.moocmini.base.exception.ValidationGroups;
import com.checo.moocmini.base.model.PageParams;
import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.content.model.dto.AddCourseDto;
import com.checo.moocmini.content.model.dto.CourseBaseInfoDto;
import com.checo.moocmini.content.model.dto.EditCourseDto;
import com.checo.moocmini.content.model.dto.QueryCourseParamsDto;
import com.checo.moocmini.content.model.po.CourseBase;
import com.checo.moocmini.content.service.CourseBaseInfoService;
import com.checo.moocmini.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程分页查询接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAnyAuthority('moocmini_teachmanager_course_list')")
    public PageResult<CourseBase> list(
            PageParams pageParams,
            @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {

        // 获取当前用户信息
        SecurityUtil.MoocminiUser moocminiUser = SecurityUtil.getUser();
        Long companyId = null;
        if (StringUtils.isNotEmpty(moocminiUser.getCompanyId())) {
            companyId = Long.parseLong(moocminiUser.getCompanyId());
        }
        return courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParamsDto);
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto) {
        // 获取用户所属机构 id
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("根据课程 id 查询课程")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseById(@PathVariable Long courseId) {
        // 获取当前用户身份
        SecurityUtil.MoocminiUser moocminiUser = SecurityUtil.getUser();
        return courseBaseInfoService.getCourseBaseInfoById(courseId);
    }

    @ApiOperation("修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto) {
        // 获取用户所属机构 id
        Long companyId = 1232141425L;
        return courseBaseInfoService.modifyCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId) {
        courseBaseInfoService.deleteCourse(courseId);
    }
}
