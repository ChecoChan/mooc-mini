package com.checo.moocmini.media.service;


import com.checo.moocmini.base.model.PageParams;
import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.base.model.RestResponse;
import com.checo.moocmini.media.model.dto.QueryMediaParamsDto;
import com.checo.moocmini.media.model.dto.UploadFileParamsDto;
import com.checo.moocmini.media.model.dto.UploadFileResultDto;
import com.checo.moocmini.media.model.po.MediaFiles;

import java.io.File;

/**
 * 媒资文件管理业务类
 */
public interface MediaFileService {

    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    /**
     * 上传文件
     *
     * @param companyId           机构 id
     * @param uploadFileResultDto 文件信息
     * @param localFilePath       文件本地路径
     * @param objectName 如果传入了 objectName 要按照 objectName 的目录存储，如果没有传入则按照年/月/日结构存储
     */
    UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileResultDto, String localFilePath, String objectName);

    /**
     * 上传文件到数据库
     */
    MediaFiles addMediaFilesToDB(Long companyId, String filedMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);

    /**
     * 上传文件到 MinIO
     */
    boolean uploadMediaFileToMinIO(String localFilePath, String mimeType, String bucket, String objectName);

    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件 MD5 值
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块是否存在
     *
     * @param fileMd5    文件 MD5 值
     * @param chunkIndex 分块序号
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块
     *
     * @param fileMd5            文件 MD5 值
     * @param chunk              分块序号
     * @param localChunkFilePath 分块文件本地路径
     */
    RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath);

    /**
     * 合并分块
     *
     * @param companyId           机构id
     * @param fileMd5             文件md5
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     */
    RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    /**
     * 从 Minio 下载文件
     *
     * @param bucket     桶
     * @param objectName 对象名称
     */
    File downloadFileFromMinIO(String bucket, String objectName);

    MediaFiles getFileById(String mediaId);
}
