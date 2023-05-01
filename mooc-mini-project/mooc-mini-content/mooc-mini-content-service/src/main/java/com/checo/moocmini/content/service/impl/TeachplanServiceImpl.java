package com.checo.moocmini.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.content.mapper.TeachplanMapper;
import com.checo.moocmini.content.mapper.TeachplanMediaMapper;
import com.checo.moocmini.content.model.dto.BindTeachplanMediaDto;
import com.checo.moocmini.content.model.dto.SaveTeachplanDto;
import com.checo.moocmini.content.model.dto.TeachplanDto;
import com.checo.moocmini.content.model.po.Teachplan;
import com.checo.moocmini.content.model.po.TeachplanMedia;
import com.checo.moocmini.content.service.TeachplanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    @Transactional
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        // 通过课程计划 id 判断是新增还是修改
        Long teachplanId = saveTeachplanDto.getId();
        Teachplan teachplan;
        if (teachplanId == null) {
            // 新增
            teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            Long courseId = teachplan.getCourseId();
            Long parentid = teachplan.getParentid();
            Long count = getTeachplanCount(courseId, parentid);
            teachplan.setOrderby((int) (count + 1));
            int insert = teachplanMapper.insert(teachplan);
            if (insert == 0)
                MoocMiniException.castException("新增失败");
        } else {
            // 修改
            teachplan = teachplanMapper.selectById(teachplanId);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            int update = teachplanMapper.updateById(teachplan);
            if (update == 0)
                MoocMiniException.castException("修改失败");
        }
    }

    private Long getTeachplanCount(Long courseId, Long parentid) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId).eq(Teachplan::getParentid, parentid);
        return teachplanMapper.selectCount(queryWrapper);
    }

    @Override
    @Transactional
    public void deleteTeachplan(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null)
            MoocMiniException.castException("删除失败");
        // 判断是删除大章节还是小章节
        if (teachplan.getGrade() == 1) {
            // 删除的是大章节
            // 如果大章节下存在小章节，则不允许删除
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, teachplan.getId());
            Long count = teachplanMapper.selectCount(queryWrapper);
            if (count > 0)
                MoocMiniException.castException("大章节下有小章节时不允许删除");
            // 大章节不存在小章节，允许删除
            int delete = teachplanMapper.deleteById(teachplanId);
            if (delete == 0)
                MoocMiniException.castException("删除失败");
        } else {
            // 删除的是小章节
            int deleteTeachplan = teachplanMapper.deleteById(teachplanId);
            if (deleteTeachplan == 0)
                MoocMiniException.castException("删除失败");
            // 如果 teachplan_media 表有关联的信息，也一并删除
            teachplanMediaMapper.deleteById(teachplanId);
        }
    }

    @Override
    @Transactional
    public void moveDown(Long teachplanId) {
        Teachplan teachplan_1 = teachplanMapper.selectById(teachplanId);
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        // 移动的是大章节
        if (teachplan_1.getGrade() == 1) {
            queryWrapper.eq(Teachplan::getCourseId, teachplan_1.getCourseId())
                    .eq(Teachplan::getGrade, teachplan_1.getGrade())
                    .eq(Teachplan::getOrderby, teachplan_1.getOrderby() + 1);
        }
        // 移动的是小章节
        if (teachplan_1.getGrade() == 2) {
            queryWrapper.eq(Teachplan::getParentid, teachplan_1.getParentid())
                    .eq(Teachplan::getOrderby, teachplan_1.getOrderby() + 1);
        }
        Teachplan teachplan_2 = teachplanMapper.selectOne(queryWrapper);
        if (teachplan_2 == null)
            MoocMiniException.castException("不可再下移");
        swapTeachplanOrderby(teachplan_1, teachplan_2);
        teachplanMapper.updateById(teachplan_1);
        teachplanMapper.updateById(teachplan_2);
    }

    @Override
    @Transactional
    public void moveUp(Long teachplanId) {
        Teachplan teachplan_1 = teachplanMapper.selectById(teachplanId);
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        // 移动的是大章节
        if (teachplan_1.getGrade() == 1) {
            queryWrapper.eq(Teachplan::getCourseId, teachplan_1.getCourseId())
                    .eq(Teachplan::getGrade, teachplan_1.getGrade())
                    .eq(Teachplan::getOrderby, teachplan_1.getOrderby() - 1);
        }
        // 移动的是小章节
        if (teachplan_1.getGrade() == 2) {
            queryWrapper.eq(Teachplan::getParentid, teachplan_1.getParentid())
                    .eq(Teachplan::getOrderby, teachplan_1.getOrderby() - 1);
        }
        Teachplan teachplan_2 = teachplanMapper.selectOne(queryWrapper);
        if (teachplan_2 == null)
            MoocMiniException.castException("不可再上移");
        swapTeachplanOrderby(teachplan_1, teachplan_2);
        teachplanMapper.updateById(teachplan_1);
        teachplanMapper.updateById(teachplan_2);
    }

    private void swapTeachplanOrderby(Teachplan teachplan_1, Teachplan teachplan_2) {
        Integer temp = teachplan_1.getOrderby();
        teachplan_1.setOrderby(teachplan_2.getOrderby());
        teachplan_2.setOrderby(temp);
    }

    @Override
    @Transactional
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null)
            MoocMiniException.castException("课程计划不存在");


        // 删除原有记录
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId, bindTeachplanMediaDto.getTeachplanId());
        teachplanMediaMapper.delete(queryWrapper);

        // 添加新纪录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMediaMapper.insert(teachplanMedia);
    }
}
