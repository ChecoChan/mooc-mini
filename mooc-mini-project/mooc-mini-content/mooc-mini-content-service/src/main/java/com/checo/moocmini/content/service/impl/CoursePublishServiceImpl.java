package com.checo.moocmini.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.checo.moocmini.base.exception.CommonError;
import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.content.config.MultipartSupportConfig;
import com.checo.moocmini.content.feignclient.MediaServiceClient;
import com.checo.moocmini.content.mapper.CourseBaseMapper;
import com.checo.moocmini.content.mapper.CourseMarketMapper;
import com.checo.moocmini.content.mapper.CoursePublishMapper;
import com.checo.moocmini.content.mapper.CoursePublishPreMapper;
import com.checo.moocmini.content.model.dto.CourseBaseInfoDto;
import com.checo.moocmini.content.model.dto.CoursePreviewDto;
import com.checo.moocmini.content.model.dto.TeachplanDto;
import com.checo.moocmini.content.model.po.CourseBase;
import com.checo.moocmini.content.model.po.CourseMarket;
import com.checo.moocmini.content.model.po.CoursePublish;
import com.checo.moocmini.content.model.po.CoursePublishPre;
import com.checo.moocmini.content.service.CourseBaseInfoService;
import com.checo.moocmini.content.service.CoursePublishService;
import com.checo.moocmini.content.service.TeachplanService;
import com.checo.moocmini.messagesdk.model.po.MqMessage;
import com.checo.moocmini.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 添加课程基本信息，营销信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseInfoById(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        // 课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanTree);

        return coursePreviewDto;
    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        // 课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        if (auditStatus.equals("202003"))
            MoocMiniException.castException("当前为等待审核状态，审核完成可以再次提交");

        // 课程机构
        Long courseBaseCompanyId = courseBase.getCompanyId();
        if (!courseBaseCompanyId.equals(companyId))
            MoocMiniException.castException("不允许提交其它机构的课程");

        // 课程图片
        String pic = courseBase.getPic();
        if (StringUtils.isEmpty(pic))
            MoocMiniException.castException("提交失败，请上传课程图片");

        // 查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.size() == 0)
            MoocMiniException.castException("提交失败，还没有添加课程计划");

        // 添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfoById(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        String teachplanTreeString = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeString);
        coursePublishPre.setStatus("202003"); // 设置预发布记录状态，已提交
        coursePublishPre.setCompanyId(companyId); // 教学机构 id
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre updateOrInsert = coursePublishPreMapper.selectById(courseId);
        if (updateOrInsert == null)
            coursePublishPreMapper.insert(coursePublishPre);
        else
            coursePublishPreMapper.updateById(coursePublishPre);

        // 更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {
        // 查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        // 需提交审核的课程才可发布
        if (coursePublishPre == null)
            MoocMiniException.castException("请先提交课程审核，审核通过才可以发布");
        // 课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        // 审核通过方才可发布
        if (!auditStatus.equals("202004"))
            MoocMiniException.castException("操作失败，课程审核通过方可发布");
        // 本机构只允许提交本机构的课程
        Long coursePublishPreCompanyId = coursePublishPre.getCompanyId();
        if (!coursePublishPreCompanyId.equals(companyId))
            MoocMiniException.castException("不允许提交其它机构的课程");

        // 保存课程发布信息
        saveCoursePublish(courseId);

        // 保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
    }


    /**
     * 保存课程发布信息
     *
     * @param courseId 课程 id
     */
    private void saveCoursePublish(Long courseId) {
        // 查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        // 需提交审核的课程才可发布
        if (coursePublishPre == null)
            MoocMiniException.castException("请先提交课程审核，审核通过才可以发布");
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish updateOrInsert = coursePublishMapper.selectById(coursePublish.getId());
        if (updateOrInsert == null)
            coursePublishMapper.insert(coursePublish);
        else
            coursePublishMapper.updateById(coursePublish);

        // 更新课程基本信息表发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 保存消息表记录
     *
     * @param courseId 课程id
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null)
            MoocMiniException.castException(CommonError.UNKNOWN_ERROR);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        File htmlFile = null;
        try {
            // 配置 freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            // 加载模板
            // 选指定模板路径，classpath/templates/
            // 得到 classpath 路径
            String classpath = Objects.requireNonNull(this.getClass().getResource("/")).getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            // 设置字符编码
            configuration.setDefaultEncoding("utf-8");

            // 指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            // 准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            // 静态化
            // 参数 1：模板，参数 2：数据模型
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            // 将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            htmlFile = File.createTempFile("course", ".html");
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("页面静态化失败，课程 id：{}", courseId);
            e.printStackTrace();
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        try {
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
            if (upload == null) {
                log.debug("远程调用媒资服务熔断，走了降级逻辑，上传结果为 null，课程 id：{}", courseId);
                MoocMiniException.castException("上传静态文件存在异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            MoocMiniException.castException("上传静态文件存在异常");
        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        // 查询缓存
        String jsonString = Objects.requireNonNull(redisTemplate.opsForValue().get("course:" + courseId)).toString();
        if (jsonString != null) {
            log.info("从缓存查询发布课程信息");
            if (jsonString.equals("null"))
                return null;
            return JSON.parseObject(jsonString, CoursePublish.class);
        } else {
            log.info("缓存为空，开始从数据库查询数据");
            // 每门课程设置一个锁
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            // 获取锁
            lock.lock();
            try {
                jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
                if (StringUtils.isNotEmpty(jsonString))
                    return JSON.parseObject(jsonString, CoursePublish.class);
                // 从数据库查询
                CoursePublish coursePublish = getCoursePublish(courseId);
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 1, TimeUnit.DAYS);
                return coursePublish;
            } finally {
                // 释放锁
                lock.unlock();
            }
        }
    }

}
