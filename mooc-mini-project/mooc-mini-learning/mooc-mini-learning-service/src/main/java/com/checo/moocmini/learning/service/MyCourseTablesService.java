package com.checo.moocmini.learning.service;

import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.learning.model.dto.MoocminiChooseCourseDto;
import com.checo.moocmini.learning.model.dto.MoocminiCourseTablesDto;
import com.checo.moocmini.learning.model.dto.MyCourseTableParams;
import com.checo.moocmini.learning.model.po.MoocminiCourseTables;

/**
 * 我的课程表服务
 */
public interface MyCourseTablesService {

    /**
     * 添加选课
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return com.checo.moocmini.learning.model.dto.MoocminiChooseCourseDto
     */
    MoocminiChooseCourseDto addChooseCourse(String userId, Long courseId);

    /**
     * 判断学习资格
     *
     * @param userId   用户id
     * @param courseId 课程id
     * @return MoocminiCourseTablesDto 学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     */
    MoocminiCourseTablesDto getLearningStatus(String userId, Long courseId);

    /**
     * 保存选课成功
     */
    boolean saveChooseCourseSuccess(String chooseCourseId);

    /**
     * 我的课程表
     */
    PageResult<MoocminiCourseTables> myCourseTables(MyCourseTableParams params);

}
