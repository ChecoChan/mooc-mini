package com.checo.moocmini.learning.api;

import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.learning.model.dto.MyCourseTableParams;
import com.checo.moocmini.learning.model.dto.MoocminiChooseCourseDto;
import com.checo.moocmini.learning.model.dto.MoocminiCourseTablesDto;
import com.checo.moocmini.learning.model.po.MoocminiCourseTables;
import com.checo.moocmini.learning.service.MyCourseTablesService;
import com.checo.moocmini.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 我的课程表接口
 */
@Slf4j
@RestController
@Api(value = "我的课程表接口", tags = "我的课程表接口")
public class MyCourseTablesController {

    @Autowired
    private MyCourseTablesService myCourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public MoocminiChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        // 获取当前登录的用户
        SecurityUtil.MoocminiUser moocminiUser = SecurityUtil.getUser();
        if (moocminiUser == null)
            MoocMiniException.castException("请登录");
        String userId = moocminiUser.getId();
        // 添加选课
        return myCourseTablesService.addChooseCourse(userId, courseId);
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public MoocminiCourseTablesDto getLearnStatus(@PathVariable("courseId") Long courseId) {
        // 获取当前登录的用户
        SecurityUtil.MoocminiUser moocminiUser = SecurityUtil.getUser();
        if (moocminiUser == null)
            MoocMiniException.castException("请登录后继续选课");

        String userId = moocminiUser.getId();
        return myCourseTablesService.getLearningStatus(userId, courseId);
    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<MoocminiCourseTables> myCourseTables(MyCourseTableParams params) {
        //登录用户
        SecurityUtil.MoocminiUser moocminiUser = SecurityUtil.getUser();
        if (moocminiUser == null)
            MoocMiniException.castException("请登录后继续选课");

        String userId = moocminiUser.getId();
        // 设置当前的登录用户
        params.setUserId(userId);
        return myCourseTablesService.myCourseTables(params);

    }
}
