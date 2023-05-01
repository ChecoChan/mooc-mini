package com.checo.moocmini.learning.api;

import com.checo.moocmini.base.model.RestResponse;
import com.checo.moocmini.learning.service.LearningService;
import com.checo.moocmini.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 我的学习接口
 */
@Api(value = "学习过程管理接口", tags = "学习过程管理接口")
@Slf4j
@RestController
public class MyLearningController {

    @Autowired
    private LearningService learningService;

    @ApiOperation("获取视频")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getVideo(@PathVariable("courseId") Long courseId, @PathVariable("teachplanId") Long teachplanId, @PathVariable("mediaId") String mediaId) {
        // 当前登录用户
        SecurityUtil.MoocminiUser moocminiUser = SecurityUtil.getUser();
        String userId = null;
        if (moocminiUser != null) {
            userId = moocminiUser.getId();
        }

        // 获取视频
        return learningService.getVideo(userId, courseId, teachplanId, mediaId);
    }
}
