package com.checo.moocmini.learning.service.impl;

import com.checo.moocmini.base.model.RestResponse;
import com.checo.moocmini.content.model.po.CoursePublish;
import com.checo.moocmini.learning.feignclient.ContentServiceClient;
import com.checo.moocmini.learning.feignclient.MediaServiceClient;
import com.checo.moocmini.learning.model.dto.MoocminiCourseTablesDto;
import com.checo.moocmini.learning.service.LearningService;
import com.checo.moocmini.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    private MyCourseTablesService myCourseTablesService;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        // 查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursePublish(courseId);
        if (coursepublish == null) {
            RestResponse.validfail("课程不存在");
        }
        // todo:是否可以试学

        // 用户已登录
        if (StringUtils.isNotEmpty(userId)) {
            // 取学习资格
            MoocminiCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);
            // 学习资格，[{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            String learnStatus = learningStatus.getLearnStatus();
            if (learnStatus.equals("702001")) {
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            } else if (learnStatus.equals("702002")) {
                return RestResponse.validfail("无法观看，由于没有选课或选课后没有支付");
            } else if (learnStatus.equals("702003")) {
                return RestResponse.validfail("您的选课已过期需要申请续期或重新支付");
            }
        }
        // 未登录或未选课判断是否收费
        String charge = coursepublish.getCharge();
        if (charge.equals("201000")) {//免费可以正常学习
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }

        return RestResponse.validfail("请购买课程后继续学习");

    }
}
