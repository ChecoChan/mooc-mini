package com.checo.moocmini.media.service;

import com.checo.moocmini.media.model.po.MediaProcess;

import java.util.List;

public interface MediaProcessService {

    /**
     * 获取待处理任务
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取记录数
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * 开启一个任务
     *
     * @param id 任务id
     * @return true 开启任务成功，false 开启任务失败
     */
    boolean startTask(long id);

    /**
     * 保存任务结果
     *
     * @param taskId   任务id
     * @param status   任务状态
     * @param fileId   文件id
     * @param url      url
     * @param errorMsg 错误信息
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);
}
