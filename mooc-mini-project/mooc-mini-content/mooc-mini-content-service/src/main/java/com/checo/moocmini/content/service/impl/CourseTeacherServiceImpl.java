package com.checo.moocmini.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.content.mapper.CourseTeacherMapper;
import com.checo.moocmini.content.model.po.CourseTeacher;
import com.checo.moocmini.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> getCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public CourseTeacher addOrUpdateCourseTeacher(CourseTeacher courseTeacher) {
        CourseTeacher courseTeacherById = courseTeacherMapper.selectById(courseTeacher.getId());
        if (courseTeacherById == null) {
            courseTeacher.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(courseTeacher);
            if (insert == 0)
                MoocMiniException.castException("添加教师失败");
        } else {
            int update = courseTeacherMapper.updateById(courseTeacher);
            if (update == 0)
                MoocMiniException.castException("修改教师失败");
        }
        return courseTeacher;
    }

    @Override
    @Transactional
    public void deleteCourseTeacher(Long courseId, Long courseTeacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, courseTeacherId);
        int delete = courseTeacherMapper.delete(queryWrapper);
        if (delete == 0)
            MoocMiniException.castException("删除失败");
    }
}
