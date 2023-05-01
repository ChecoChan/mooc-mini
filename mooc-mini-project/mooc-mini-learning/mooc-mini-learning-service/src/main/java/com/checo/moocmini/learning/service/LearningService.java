package com.checo.moocmini.learning.service;

import com.checo.moocmini.base.model.RestResponse;

public interface LearningService {

    /**
     * 获取教学视频
     *
     * @param courseId    课程id
     * @param teachplanId 课程计划id
     * @param mediaId     视频文件id
     * @return com.checo.moocmini.base.model.RestResponse<java.lang.String>
     */
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
