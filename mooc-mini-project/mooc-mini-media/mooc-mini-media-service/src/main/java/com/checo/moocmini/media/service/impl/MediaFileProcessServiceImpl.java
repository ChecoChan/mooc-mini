package com.checo.moocmini.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.checo.moocmini.media.mapper.MediaFilesMapper;
import com.checo.moocmini.media.mapper.MediaProcessHistoryMapper;
import com.checo.moocmini.media.mapper.MediaProcessMapper;
import com.checo.moocmini.media.model.po.MediaFiles;
import com.checo.moocmini.media.model.po.MediaProcess;
import com.checo.moocmini.media.model.po.MediaProcessHistory;
import com.checo.moocmini.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaProcessService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result != 0;
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        // 查出任务，如果不存在则直接返回
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null)
            return;

        // 处理失败
        if (status.equals("3")) {
            LambdaQueryWrapper<MediaProcess> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MediaProcess::getId, taskId);
            MediaProcess mediaProcess_u = new MediaProcess();
            mediaProcess_u.setStatus("3");
            mediaProcess_u.setErrormsg(errorMsg);
            mediaProcess_u.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcessMapper.update(mediaProcess_u, queryWrapper);
            log.debug("更新任务处理状态为失败，任务信息：{}", mediaProcess_u);
            return;
        }

        // 任务处理成功
        MediaFiles mediaFile = mediaFilesMapper.selectById(fileId);
        if (mediaFile != null) {
            // 更新媒资文件中的访问 url
            mediaFile.setUrl(url);
            mediaFilesMapper.updateById(mediaFile);
        }
        // 处理成功，更新 url 和状态
        mediaProcess.setUrl(url);
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcessMapper.updateById(mediaProcess);
        // 添加到历史记录
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        // 删除 mediaProcess
        mediaProcessMapper.deleteById(mediaProcess.getId());
    }
}
