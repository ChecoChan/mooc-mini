package com.checo.moocmini.content.service;

import com.checo.moocmini.content.model.po.CourseTeacher;

import java.util.List;

/**
 * 课程教师管理相关接口
 */
public interface CourseTeacherService {
    List<CourseTeacher> getCourseTeacher(Long courseId);

    CourseTeacher addOrUpdateCourseTeacher(CourseTeacher courseTeacher);

    void deleteCourseTeacher(Long courseId, Long courseTeacherId);
}
