package com.checo.moocmini.content.service;

import com.checo.moocmini.base.model.PageParams;
import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.content.model.dto.AddCourseDto;
import com.checo.moocmini.content.model.dto.CourseBaseInfoDto;
import com.checo.moocmini.content.model.dto.EditCourseDto;
import com.checo.moocmini.content.model.dto.QueryCourseParamsDto;
import com.checo.moocmini.content.model.po.CourseBase;

/**
 * 课程信息管理接口
 */
public interface CourseBaseInfoService {

    /**
     * 课程分页查询
     *
     * @param companyId            机构 id
     * @param pageParams           分页查询的参数
     * @param queryCourseParamsDto 查询条件
     * @return 查询结果
     */
    PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程
     *
     * @param companyId    机构 id
     * @param addCourseDto 添加课程信息
     * @return 课程添加成功详细信息
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);


    /**
     * 根据课程 id 查询课程详细信息
     *
     * @param courseId 课程 id
     * @return 课程详细信息
     */
    CourseBaseInfoDto getCourseBaseInfoById(Long courseId);


    /**
     * 修改课程
     *
     * @param companyId     机构 id
     * @param editCourseDto 修改课程信息
     * @return 课程详细信息
     */
    CourseBaseInfoDto modifyCourseBase(Long companyId, EditCourseDto editCourseDto);


    /**
     * 删除课程
     *
     * @param courseId 课程 id
     */
    void deleteCourse(Long courseId);
}
