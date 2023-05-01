package com.checo.moocmini.content.service.jobhandler;


import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.content.feignclient.SearchServiceClient;
import com.checo.moocmini.content.mapper.CoursePublishMapper;
import com.checo.moocmini.content.model.dto.CoursePreviewDto;
import com.checo.moocmini.content.model.po.CourseIndex;
import com.checo.moocmini.content.model.po.CoursePublish;
import com.checo.moocmini.content.service.CoursePublishService;
import com.checo.moocmini.messagesdk.model.po.MqMessage;
import com.checo.moocmini.messagesdk.service.MessageProcessAbstract;
import com.checo.moocmini.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    private CoursePublishService coursePublishService;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private SearchServiceClient searchServiceClient;

    /**
     * 任务调度入口
     */
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex = " + shardIndex + ",shardTotal = " + shardTotal);
        // 参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }


    /**
     * 课程发布任务处理
     *
     * @param mqMessage 执行任务内容
     */
    @Override
    public boolean execute(MqMessage mqMessage) {
        // 获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Long.parseLong(businessKey1);
        // 课程静态化
        generateCourseHtml(mqMessage, courseId);
        // 课程索引
        saveCourseIndex(mqMessage, courseId);
        // 课程缓存
        saveCourseCache(mqMessage, courseId);
        return true;
    }

    /**
     * 生成课程静态化页面并上传至文件系统
     *
     * @param mqMessage 执行任务内容
     * @param courseId  课程 id
     */
    private void generateCourseHtml(MqMessage mqMessage, long courseId) {

        log.debug("开始进行课程静态化,课程 id：{}", courseId);
        // 消息 id
        Long messageId = mqMessage.getId();
        // 消息处理的 service
        MqMessageService mqMessageService = this.getMqMessageService();
        // 消息幂等性处理
        int stageOne = mqMessageService.getStageOne(messageId);
        if (stageOne > 0) {
            log.debug("课程静态化已处理直接返回，课程 id：{}", courseId);
            return;
        }
        // 开始课程静态化，生成 HTML 页面
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null)
            MoocMiniException.castException("生成静态页面为 null");
        // 将 HTML 页面上传到 MinIO
        coursePublishService.uploadCourseHtml(courseId, file);

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 保存第一阶段状态
        mqMessageService.completedStageOne(messageId);
    }

    /**
     * 将课程信息缓存至 redis
     *
     * @param mqMessage 执行任务内容
     * @param courseId  课程 id
     */
    private void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("将课程信息缓存至 redis ,课程 id:{}", courseId);
        Long messageId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(messageId);
        if (stageTwo > 0) {
            log.debug("课程已缓存，课程 id：{}", courseId);
            return;
        }
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mqMessageService.completedStageTwo(messageId);
    }

    /**
     * 保存课程索引信息
     *
     * @param mqMessage 执行任务内容
     * @param courseId  课程 id
     */
    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("保存课程索引信息,课程id:{}", courseId);
        Long messageId = mqMessage.getId();
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(messageId);
        if (stageThree > 0) {
            log.debug("课程索引信息已写入，课程 id：{}", courseId);
            return;
        }

        // 查询已发布课程信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        // 远程调用课程索引服务
        Boolean add = searchServiceClient.add(courseIndex);
        if (!add)
            MoocMiniException.castException("远程调用课程索引服务失败");

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 保存第三阶段状态
        mqMessageService.completedStageThree(messageId);
    }
}
