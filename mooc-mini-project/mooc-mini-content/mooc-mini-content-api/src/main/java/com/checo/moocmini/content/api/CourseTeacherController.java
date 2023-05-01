package com.checo.moocmini.content.api;

import com.checo.moocmini.content.model.po.CourseTeacher;
import com.checo.moocmini.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程教师管理接口", tags = "课程教师管理接口")
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @ApiOperation("查询教师接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeacher(@PathVariable Long courseId) {
        return courseTeacherService.getCourseTeacher(courseId);
    }

    @ApiOperation("添加或修改教师接口")
    @PostMapping("/courseTeacher")
    public CourseTeacher addOrUpdateCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.addOrUpdateCourseTeacher(courseTeacher);
    }

    @ApiOperation("删除教师接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{courseTeacherId}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long courseTeacherId) {
        courseTeacherService.deleteCourseTeacher(courseId, courseTeacherId);
    }
}
