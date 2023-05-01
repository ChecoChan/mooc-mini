package com.checo.moocmini.content.service;

import com.checo.moocmini.content.model.dto.CoursePreviewDto;
import com.checo.moocmini.content.model.po.CoursePublish;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;

/**
 * 课程预览、发布接口
 */
public interface CoursePublishService {

    /**
     * 获取课程预览信息
     *
     * @param courseId 课程id
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * 提交审核
     *
     * @param courseId 课程id
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布接口
     *
     * @param companyId 机构id
     * @param courseId  课程id
     */
    void publish(Long companyId, Long courseId);

    /**
     * 课程静态化
     *
     * @param courseId 课程id
     */
    File generateCourseHtml(Long courseId);

    /**
     * 上传课程静态化页面
     *
     * @param courseId 课程id
     * @param file     静态化文件
     */
    void uploadCourseHtml(Long courseId, File file);

    /**
     * 查询课程发布信息
     */
    CoursePublish getCoursePublish(Long courseId);

    /**
     * 查询缓存中的课程信息
     */
    CoursePublish getCoursePublishCache(Long courseId);

}
