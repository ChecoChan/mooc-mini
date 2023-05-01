package com.checo.moocmini.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.base.model.PageParams;
import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.content.mapper.*;
import com.checo.moocmini.content.model.dto.AddCourseDto;
import com.checo.moocmini.content.model.dto.CourseBaseInfoDto;
import com.checo.moocmini.content.model.dto.EditCourseDto;
import com.checo.moocmini.content.model.dto.QueryCourseParamsDto;
import com.checo.moocmini.content.model.po.*;
import com.checo.moocmini.content.service.CourseBaseInfoService;
import com.checo.moocmini.content.service.CoursePublishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        // 拼装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // 根据课程名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        // 根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());
        // 根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()), CourseBase::getStatus, courseParamsDto.getPublishStatus());
        // 根据机构 id 查询
        queryWrapper.eq(companyId != null, CourseBase::getCompanyId, companyId);

        Page<CourseBase> courseBasePage = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(courseBasePage, queryWrapper);
        List<CourseBase> items = pageResult.getRecords();
        Long total = pageResult.getTotal();
        return new PageResult<>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        // 参数的合法性校验 -- 已经使用 JSR303 统一校验，spring-boot-starter-validation 提供依赖支持
        // if (StringUtils.isBlank(addCourseDto.getName()))
        //     MoocMiniException.castException("课程名称为空");
        // if (StringUtils.isBlank(addCourseDto.getMt()))
        //     MoocMiniException.castException("课程分类为空");
        // if (StringUtils.isBlank(addCourseDto.getSt()))
        //     MoocMiniException.castException("课程分类为空");
        // if (StringUtils.isBlank(addCourseDto.getGrade()))
        //     MoocMiniException.castException("课程等级为空");
        // if (StringUtils.isBlank(addCourseDto.getTeachmode()))
        //     MoocMiniException.castException("教育模式为空");
        // if (StringUtils.isBlank(addCourseDto.getUsers()))
        //     MoocMiniException.castException("适应人群为空");
        // if (StringUtils.isBlank(addCourseDto.getCharge()))
        //     MoocMiniException.castException("收费规则为空");

        // 向课程基本信息表 course_base 写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002"); // 审核状态默认为未提交
        courseBase.setStatus("203001"); // 发布状态默认为未发布
        int insertCourseBase = courseBaseMapper.insert(courseBase);
        if (insertCourseBase == 0)
            MoocMiniException.castException("添加课程失败");

        // 向课程营销表 course_market 写入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        Long courseId = courseBase.getId();
        courseMarket.setId(courseId);
        int insertCourseMarket = saveCourseMarket(courseMarket);
        if (insertCourseMarket == 0)
            MoocMiniException.castException("添加课程失败");

        return getCourseBaseInfoById(courseId);
    }

    // 保存课程的营销信息：课程存在则更新，课程不存在则添加
    private int saveCourseMarket(CourseMarket courseMarket) {
        // 参数的合法性校验
        String charge = courseMarket.getCharge();
        if (StringUtils.isEmpty(charge))
            MoocMiniException.castException("收费规则为空");
        if (charge.equals("201001")) { // 201001 为收费课程
            Float price = courseMarket.getPrice();
            if (price == null || price <= 0)
                MoocMiniException.castException("收费课程价格不能为空或小于 0");
        }

        // 从数据库查询营销信息
        Long id = courseMarket.getId();
        CourseMarket courseMarketById = courseMarketMapper.selectById(id);
        if (courseMarketById == null) {
            // 添加数据
            int insert = courseMarketMapper.insert(courseMarket);
            return insert;
        } else {
            // 更新数据
            BeanUtils.copyProperties(courseMarket, courseMarketById);
            courseMarketById.setId(courseMarket.getId());
            int update = courseMarketMapper.updateById(courseMarketById);
            return update;
        }
    }

    // 根据 id 查询课程信息
    @Override
    public CourseBaseInfoDto getCourseBaseInfoById(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null)
            return null;
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket != null)
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);

        String mtName = courseCategoryMapper.selectById(courseBase.getMt()).getName();
        String stName = courseCategoryMapper.selectById(courseBase.getSt()).getName();
        courseBaseInfoDto.setMtName(mtName);
        courseBaseInfoDto.setStName(stName);

        return courseBaseInfoDto;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto modifyCourseBase(Long companyId, EditCourseDto editCourseDto) {

        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null)
            MoocMiniException.castException("课程不存在");

        // 本机构只能修改本机构的课程
        if (!companyId.equals(courseBase.getCompanyId()))
            MoocMiniException.castException("只允许修改本机构的课程");

        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int updateCourseBase = courseBaseMapper.updateById(courseBase);
        if (updateCourseBase == 0)
            MoocMiniException.castException("修改课程失败");

        CourseMarket courseMarket = courseMarketMapper.selectById(courseBase.getId());
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        int updateCourseMarket = courseMarketMapper.updateById(courseMarket);
        if (updateCourseMarket == 0)
            MoocMiniException.castException("修改课程失败");

        return getCourseBaseInfoById(courseId);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null)
            MoocMiniException.castException("删除课程失败");

        // 删除课程基本信息
        int deleteCourseBase = courseBaseMapper.deleteById(courseId);
        if (deleteCourseBase == 0)
            MoocMiniException.castException("删除课程失败");

        // 删除课程营销信息
        courseMarketMapper.deleteById(courseId);

        // 删除课程计划
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, courseId);
        List<Teachplan> teachplans = teachplanMapper.selectList(teachplanLambdaQueryWrapper);
        if (teachplans.size() > 0)
            teachplanMapper.deleteBatchIds(teachplans);

        // 删除课程计划关联信息
        LambdaQueryWrapper<TeachplanMedia> teachplanMediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanMediaLambdaQueryWrapper.eq(TeachplanMedia::getCourseId, courseId);
        List<TeachplanMedia> teachplanMedias = teachplanMediaMapper.selectList(teachplanMediaLambdaQueryWrapper);
        if (teachplanMedias.size() > 0)
            teachplanMediaMapper.deleteBatchIds(teachplanMedias);

        // 删除课程教师
        LambdaQueryWrapper<CourseTeacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(teacherLambdaQueryWrapper);
        if (courseTeachers.size() > 0)
            courseTeacherMapper.deleteBatchIds(courseTeachers);
    }
}
