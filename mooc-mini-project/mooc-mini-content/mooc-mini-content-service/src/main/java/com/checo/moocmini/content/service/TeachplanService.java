package com.checo.moocmini.content.service;

import com.checo.moocmini.content.model.dto.BindTeachplanMediaDto;
import com.checo.moocmini.content.model.dto.SaveTeachplanDto;
import com.checo.moocmini.content.model.dto.TeachplanDto;
import com.checo.moocmini.content.model.po.Teachplan;

import java.util.List;

/**
 * 课程计划管理相关接口
 */
public interface TeachplanService {

    List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新增大章节、小章节、修改章节信息接口
     * @param saveTeachplanDto 修改信息
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    void deleteTeachplan(Long teachplanId);

    void moveDown(Long teachplanId);

    void moveUp(Long teachplanId);

    /**
     * 教学计划绑定媒资
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
