package com.checo.moocmini.media.service.jobhandler;

import com.checo.moocmini.base.utils.Mp4VideoUtil;
import com.checo.moocmini.media.model.po.MediaProcess;
import com.checo.moocmini.media.service.MediaFileService;
import com.checo.moocmini.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 视频任务处理类
 */
@Slf4j
@Component
public class VideoTask {

    @Autowired
    MediaFileService mediaFileService;

    @Autowired
    MediaProcessService mediaProcessService;

    // ffmpeg的路径
    @Value("${videoprocess.ffmpegpath}")
    String ffmpegPath;


    /**
     * 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        // 确定 CPU 核心数
        int processors = Runtime.getRuntime().availableProcessors();

        // 查询待处理任务
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        // 任务数量
        int taskSize = mediaProcessList.size();
        log.debug("取到的视频任务数：{}", taskSize);
        if (taskSize == 0)
            return;

        // 使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(taskSize);

        // 创建线程池执行任务
        ExecutorService executorService = Executors.newFixedThreadPool(taskSize);
        mediaProcessList.forEach(mediaProcess -> {
            // 将任务加入线程池
            executorService.execute(() -> {
                try {
                    // 开启任务(通过乐观锁)
                    Long taskId = mediaProcess.getId();
                    boolean startTask = mediaProcessService.startTask(taskId);
                    if (!startTask) {
                        log.debug("抢占任务失败， 任务 id：{}", taskId);
                        return;
                    }

                    // 执行视频转码
                    // 源 avi 视频的路径
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    String fileId = mediaProcess.getFileId();// 文件 Id 就是 md5 值
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下载视频失败， 任务 id：{}，bucket：{}， objectName：{}", taskId, bucket, objectName);
                        // 保存任务处理失败的结果
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    String videoPath = file.getAbsolutePath();
                    // 转换后 mp4 文件的名称
                    String mp4Name = fileId + ".mp4";
                    // 转换后 mp4 文件的路径
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常，{}", e.getMessage());
                        // 保存任务处理失败的结果
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "保存任务处理失败的结果");
                        return;
                    }
                    String mp4Path = mp4File.getAbsolutePath();

                    // 创建工具类对象
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath, videoPath, mp4Name, mp4Path);
                    // 开始视频转换，成功将返回 success
                    String result = mp4VideoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.debug("视频转码失败，bucket：{}，objectName：{}，失败原因：{}", result, bucket, objectName);
                        // 保存任务处理失败的结果
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                    }
                    // 上传到 MinIo
                    boolean uploadMediaFileToMinIO = mediaFileService.uploadMediaFileToMinIO(mp4Path, "video/mp4", bucket, objectName);
                    if (!uploadMediaFileToMinIO) {
                        log.debug("上传 mp4 视频到 MinIO 失败，任务 id：{}", taskId);
                        // 保存任务处理失败的结果
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传 mp4 视频到 MinIO 失败");
                    }
                    String url = getFilePathByMd5(fileId, ".mp4");
                    // 保存任务成功
                    mediaProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "保存任务处理失败的结果");
                } finally {
                    // 计数器减一
                    countDownLatch.countDown();
                }
            });
        });

        // 阻塞，指定最大等待时间为 30 分钟
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePathByMd5(String fileMd5, String extensionName) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + extensionName;
    }
}
