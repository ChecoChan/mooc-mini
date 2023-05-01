package com.checo.moocmini.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.content.model.po.CoursePublish;
import com.checo.moocmini.learning.feignclient.ContentServiceClient;
import com.checo.moocmini.learning.mapper.MoocminiChooseCourseMapper;
import com.checo.moocmini.learning.mapper.MoocminiCourseTablesMapper;
import com.checo.moocmini.learning.model.dto.MoocminiChooseCourseDto;
import com.checo.moocmini.learning.model.dto.MoocminiCourseTablesDto;
import com.checo.moocmini.learning.model.dto.MyCourseTableParams;
import com.checo.moocmini.learning.model.po.MoocminiChooseCourse;
import com.checo.moocmini.learning.model.po.MoocminiCourseTables;
import com.checo.moocmini.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    private MoocminiChooseCourseMapper moocminiChooseCourseMapper;

    @Autowired
    private MoocminiCourseTablesMapper moocminiCourseTablesMapper;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private MyCourseTablesService myCourseTablesService;

    @Autowired
    private MyCourseTablesServiceImpl currentProxy;

    @Override
    @Transactional
    public MoocminiChooseCourseDto addChooseCourse(String userId, Long courseId) {
        // 查询发布课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursePublish(courseId);
        if (coursePublish == null)
            MoocMiniException.castException("课程不存在");
        // 课程收费标准
        String charge = coursePublish.getCharge();
        // 选课记录
        MoocminiChooseCourse chooseCourse;
        if (charge.equals("201000")) {//课程免费
            // 添加免费课程
            chooseCourse = addFreeCourse(userId, coursePublish);
            // 添加到我的课程表
            MoocminiCourseTables moocminiCourseTables = addCourseTables(chooseCourse);
        } else {
            // 添加收费课程
            chooseCourse = addChargeCourse(userId, coursePublish);
        }
        // 获取学习资格
        MoocminiCourseTablesDto moocminiCourseTablesDto = getLearningStatus(userId, courseId);

        // 返回所需数据
        MoocminiChooseCourseDto moocminiChooseCourseDto = new MoocminiChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse, moocminiChooseCourseDto);
        moocminiChooseCourseDto.setLearnStatus(moocminiChooseCourseDto.getLearnStatus());
        return moocminiChooseCourseDto;
    }


    /**
     * 添加免费课程,免费课程加入选课记录表、我的课程表
     */
    private MoocminiChooseCourse addFreeCourse(String userId, CoursePublish coursePublish) {
        // 查询选课记录表是否存在免费的且选课成功的订单
        LambdaQueryWrapper<MoocminiChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(MoocminiChooseCourse::getUserId, userId)
                .eq(MoocminiChooseCourse::getCourseId, coursePublish.getId())
                .eq(MoocminiChooseCourse::getOrderType, "700001") // 免费课程
                .eq(MoocminiChooseCourse::getStatus, "701001"); // 选课成功
        List<MoocminiChooseCourse> moocminiChooseCourses = moocminiChooseCourseMapper.selectList(queryWrapper);
        if (moocminiChooseCourses != null && moocminiChooseCourses.size() > 0)
            return moocminiChooseCourses.get(0);

        // 添加选课记录信息
        MoocminiChooseCourse moocminiChooseCourse = new MoocminiChooseCourse();
        moocminiChooseCourse.setCourseId(coursePublish.getId());
        moocminiChooseCourse.setCourseName(coursePublish.getName());
        moocminiChooseCourse.setCoursePrice(0f); // 免费课程价格为0
        moocminiChooseCourse.setUserId(userId);
        moocminiChooseCourse.setCompanyId(coursePublish.getCompanyId());
        moocminiChooseCourse.setOrderType("700001"); // 免费课程
        moocminiChooseCourse.setCreateDate(LocalDateTime.now());
        moocminiChooseCourse.setStatus("701001"); // 选课成功
        moocminiChooseCourse.setValidDays(365); // 免费课程默认365
        moocminiChooseCourse.setValidtimeStart(LocalDateTime.now());
        moocminiChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        moocminiChooseCourseMapper.insert(moocminiChooseCourse);

        return moocminiChooseCourse;
    }

    /**
     * 添加课程到我的课程表
     */
    public MoocminiCourseTables addCourseTables(MoocminiChooseCourse moocminiChooseCourse) {
        // 选课记录完成且未过期可以添加课程到课程表
        String status = moocminiChooseCourse.getStatus();
        if (!status.equals("701001"))
            MoocMiniException.castException("选课未成功，无法添加到课程表");

        // 查询我的课程表
        MoocminiCourseTables moocminiCourseTables = getMoocminiCourseTables(moocminiChooseCourse.getUserId(), moocminiChooseCourse.getCourseId());
        if (moocminiCourseTables != null)
            return moocminiCourseTables;

        MoocminiCourseTables moocminiCourseTablesNew = new MoocminiCourseTables();
        moocminiCourseTablesNew.setChooseCourseId(moocminiChooseCourse.getId());
        moocminiCourseTablesNew.setUserId(moocminiChooseCourse.getUserId());
        moocminiCourseTablesNew.setCourseId(moocminiChooseCourse.getCourseId());
        moocminiCourseTablesNew.setCompanyId(moocminiChooseCourse.getCompanyId());
        moocminiCourseTablesNew.setCourseName(moocminiChooseCourse.getCourseName());
        moocminiCourseTablesNew.setCreateDate(LocalDateTime.now());
        moocminiCourseTablesNew.setValidtimeStart(moocminiChooseCourse.getValidtimeStart());
        moocminiCourseTablesNew.setValidtimeEnd(moocminiChooseCourse.getValidtimeEnd());
        moocminiCourseTablesNew.setCourseType(moocminiChooseCourse.getOrderType());
        moocminiCourseTablesMapper.insert(moocminiCourseTablesNew);

        return moocminiCourseTablesNew;

    }

    /**
     * 查询我的课程表
     */
    private MoocminiCourseTables getMoocminiCourseTables(String userId, Long courseId) {
        LambdaQueryWrapper<MoocminiCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MoocminiCourseTables::getUserId, userId)
                .eq(MoocminiCourseTables::getCourseId, courseId);
        return moocminiCourseTablesMapper.selectOne(queryWrapper);
    }

    /**
     * 添加收费课程
     */
    public MoocminiChooseCourse addChargeCourse(String userId, CoursePublish coursePublish) {
        // 如果存在待支付记录直接返回
        LambdaQueryWrapper<MoocminiChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(MoocminiChooseCourse::getUserId, userId)
                .eq(MoocminiChooseCourse::getCourseId, coursePublish.getId())
                .eq(MoocminiChooseCourse::getOrderType, "700002") // 收费订单
                .eq(MoocminiChooseCourse::getStatus, "701002"); // 待支付
        List<MoocminiChooseCourse> moocminiChooseCourses = moocminiChooseCourseMapper.selectList(queryWrapper);
        if (moocminiChooseCourses != null && moocminiChooseCourses.size() > 0)
            return moocminiChooseCourses.get(0);

        MoocminiChooseCourse moocminiChooseCourse = new MoocminiChooseCourse();
        moocminiChooseCourse.setCourseId(coursePublish.getId());
        moocminiChooseCourse.setCourseName(coursePublish.getName());
        moocminiChooseCourse.setCoursePrice(coursePublish.getPrice());
        moocminiChooseCourse.setUserId(userId);
        moocminiChooseCourse.setCompanyId(coursePublish.getCompanyId());
        moocminiChooseCourse.setOrderType("700002"); // 收费课程
        moocminiChooseCourse.setCreateDate(LocalDateTime.now());
        moocminiChooseCourse.setStatus("701002"); // 待支付
        moocminiChooseCourse.setValidDays(coursePublish.getValidDays());
        moocminiChooseCourse.setValidtimeStart(LocalDateTime.now());
        moocminiChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursePublish.getValidDays()));
        moocminiChooseCourseMapper.insert(moocminiChooseCourse);

        return moocminiChooseCourse;
    }

    @Override
    public MoocminiCourseTablesDto getLearningStatus(String userId, Long courseId) {
        // 查询我的课程表
        MoocminiCourseTables moocminiCourseTables = getMoocminiCourseTables(userId, courseId);
        if (moocminiCourseTables == null) {
            MoocminiCourseTablesDto moocminiCourseTablesDto = new MoocminiCourseTablesDto();
            // 没有选课或选课后没有支付
            moocminiCourseTablesDto.setLearnStatus("702002");
            return moocminiCourseTablesDto;
        }

        MoocminiCourseTablesDto moocminiCourseTablesDto = new MoocminiCourseTablesDto();
        BeanUtils.copyProperties(moocminiCourseTables, moocminiCourseTablesDto);
        // 是否过期，true 过期，false 未过期
        boolean isExpires = moocminiCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        if (!isExpires)
            moocminiCourseTablesDto.setLearnStatus("702001"); // 正常学习
        else
            moocminiCourseTablesDto.setLearnStatus("702003"); // 已过期

        return moocminiCourseTablesDto;
    }

    @Override
    @Transactional
    public boolean saveChooseCourseSuccess(String chooseCourseId) {
        // 根据 choosecourseId 查询选课记录
        MoocminiChooseCourse moocminiChooseCourse = moocminiChooseCourseMapper.selectById(chooseCourseId);
        if (moocminiChooseCourse == null) {
            log.debug("收到支付结果通知没有查询到关联的选课记录，choosecourseId:{}", chooseCourseId);
            return false;
        }
        String status = moocminiChooseCourse.getStatus();
        if (status.equals("701001")) {
            //添加到课程表
            addCourseTables(moocminiChooseCourse);
            return true;
        }
        // 待支付状态才处理
        if (status.equals("701002")) {
            // 更新为选课成功
            moocminiChooseCourse.setStatus("701001");
            int update = moocminiChooseCourseMapper.updateById(moocminiChooseCourse);
            if (update > 0) {
                log.debug("收到支付结果通知处理成功，选课记录:{}", moocminiChooseCourse);
                //添加到课程表
                addCourseTables(moocminiChooseCourse);
                return true;
            } else {
                log.debug("收到支付结果通知处理失败，选课记录:{}", moocminiChooseCourse);
                return false;
            }
        }

        return false;
    }

    @Override
    public PageResult<MoocminiCourseTables> myCourseTables(MyCourseTableParams params) {
        // 页码
        long pageNo = params.getPage();
        // 每页记录数
        long pageSize = params.getSize();
        // 分页条件
        Page<MoocminiCourseTables> page = new Page<>(pageNo, pageSize);
        // 根据用户 id 查询
        String userId = params.getUserId();
        LambdaQueryWrapper<MoocminiCourseTables> lambdaQueryWrapper = new LambdaQueryWrapper<MoocminiCourseTables>().eq(MoocminiCourseTables::getUserId, userId);

        // 分页查询
        Page<MoocminiCourseTables> pageResult = moocminiCourseTablesMapper.selectPage(page, lambdaQueryWrapper);
        List<MoocminiCourseTables> records = pageResult.getRecords();
        // 记录总数
        long total = pageResult.getTotal();
        PageResult<MoocminiCourseTables> courseTablesResult = new PageResult<>(records, total, pageNo, pageSize);
        return courseTablesResult;
    }
}
