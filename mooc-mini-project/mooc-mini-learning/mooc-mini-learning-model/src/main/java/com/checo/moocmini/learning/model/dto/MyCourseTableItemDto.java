package com.checo.moocmini.learning.model.dto;

import com.checo.moocmini.learning.model.po.MoocminiCourseTables;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 我的课程查询条件
 */
@Data
@ToString
public class MyCourseTableItemDto extends MoocminiCourseTables {

    /**
     * 最近学习时间
     */
    private LocalDateTime learnDate;

    /**
     * 学习时长
     */
    private Long learnLength;

    /**
     * 章节id
     */
    private Long teachplanId;

    /**
     * 章节名称
     */
    private String teachplanName;
}
