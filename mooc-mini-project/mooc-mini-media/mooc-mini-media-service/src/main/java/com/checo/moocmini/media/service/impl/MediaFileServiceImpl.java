package com.checo.moocmini.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.base.model.PageParams;
import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.base.model.RestResponse;
import com.checo.moocmini.media.mapper.MediaFilesMapper;
import com.checo.moocmini.media.mapper.MediaProcessMapper;
import com.checo.moocmini.media.model.dto.QueryMediaParamsDto;
import com.checo.moocmini.media.model.dto.UploadFileParamsDto;
import com.checo.moocmini.media.model.dto.UploadFileResultDto;
import com.checo.moocmini.media.model.po.MediaFiles;
import com.checo.moocmini.media.model.po.MediaProcess;
import com.checo.moocmini.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.DeletedObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    /**
     * 存储普通媒体文件
     */
    @Value("${minio.bucket.files}")
    private String bucket_mideafiles;

    /**
     * 存储视频文件
     */
    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Autowired
    private MediaFileService currentProxy;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    public UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath, String objectName) {
        // 将文件上传到 Minio
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        String defaultFolderPath = getDefaultFolderPath();
        String fileMd5 = getFileMd5(new File(localFilePath));
        // 如果没有传入 objectName，则按照年/月/日结构存储
        if (StringUtils.isEmpty(objectName))
            objectName = defaultFolderPath + fileMd5 + extension;
        boolean uploadMediaFileToMinIO = uploadMediaFileToMinIO(localFilePath, mimeType, bucket_mideafiles, objectName);
        if (!uploadMediaFileToMinIO)
            MoocMiniException.castException("文件上传 MinIO 失败");

        // 将文件保存到数据库
        MediaFiles mediaFile = currentProxy.addMediaFilesToDB(companyId, fileMd5, uploadFileParamsDto, bucket_mideafiles, objectName);
        if (mediaFile == null)
            MoocMiniException.castException("文件信息添加数据库失败");

        // 返回前端所需对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFile, uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 根据扩展名获取 MimeType
     *
     * @param extension 扩展名
     * @return MimeType 类型
     */
    private String getMimeType(String extension) {
        if (extension == null)
            extension = "";
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null)
            mimeType = extensionMatch.getMimeType();
        return mimeType;
    }

    @Override
    public boolean uploadMediaFileToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .object(objectName)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.info("上传文件到 Minio 成功，bucket：{}，objectName:：{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到 Minio 出错，bucket：{}，objectName:：{}，错误原因：{}", bucket, objectName, e.getMessage(), e);
        }
        return false;
    }

    private String getDefaultFolderPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String folderPath = simpleDateFormat.format(new Date()).replace("-", "/") + "/";
        return folderPath;
    }

    private String getFileMd5(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            return DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public MediaFiles addMediaFilesToDB(Long companyId, String filedMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        // 将视频文件信息加入数据库 Media_files
        MediaFiles mediaFile = mediaFilesMapper.selectById(filedMd5);
        if (mediaFile == null) {
            mediaFile = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFile);
            mediaFile.setId(filedMd5);
            mediaFile.setCompanyId(companyId);
            mediaFile.setBucket(bucket);
            mediaFile.setFilePath(objectName);
            mediaFile.setFileId(filedMd5);
            mediaFile.setUrl("/" + bucket + "/" + objectName);
            mediaFile.setCreateDate(LocalDateTime.now());
            mediaFile.setStatus("1");
            mediaFile.setAuditStatus("002003");
            int insert = mediaFilesMapper.insert(mediaFile);
            if (insert == 0) {
                log.debug("想数据库保存文件信息失败，bucket:{}，objectName：{}", bucket, objectName);
                return null;
            }
            return mediaFile;
        }

        // 添加待处理的任务
        addWaitingTask(mediaFile);

        return mediaFile;
    }

    /**
     * 添加待处理任务
     * @param mediaFile 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFile) {
        // 获取文件的 MimeType
        String filename = mediaFile.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        // 如果 MimeType 是 avi 视频，则写入待处理任务表
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFile, mediaProcess);
            mediaProcess.setStatus("1"); // 未处理
            mediaProcess.setFailCount(0); // 失败次数默认为 0
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 查询文件信息
        MediaFiles mediaFile = mediaFilesMapper.selectById(fileMd5);
        if (mediaFile != null) {
            String bucket = mediaFile.getBucket();
            String filePath = mediaFile.getFilePath();
            InputStream inputStream = null;
            try {
                GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(filePath)
                        .build();
                inputStream = minioClient.getObject(getObjectArgs);
                // 文件已存在
                if (inputStream != null)
                    return RestResponse.success(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 文件不存在
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        String chunkFolderPath = getChunkFolderPath(fileMd5);
        String chunkFilePath = chunkFolderPath + chunkIndex;
        InputStream inputStream = null;
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket_videofiles)
                    .object(chunkFilePath)
                    .build();
            inputStream = minioClient.getObject(getObjectArgs);
            // 分块已存在
            if (inputStream != null)
                return RestResponse.success(true);
        } catch (Exception e) {
            log.error("发生错误");
        }
        // 分块不存在
        return RestResponse.success(false);
    }

    /**
     * 得到分块文件目录
     */
    private String getChunkFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        String mimeType = getMimeType(null);
        String chunkFolderPath = getChunkFolderPath(fileMd5);
        String chunkFilePath = chunkFolderPath + chunk;
        boolean uploadMediaFileToMinIO = uploadMediaFileToMinIO(localChunkFilePath, mimeType, bucket_videofiles, chunkFilePath);
        // 上传失败
        if (!uploadMediaFileToMinIO) {
            log.debug("上传分块失败：{}", chunkFilePath);
            return RestResponse.validfail(false, "上传分块失败");
        }
        // 上传成功
        log.debug("上传分块文件成功：{}", chunkFilePath);
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 合并文件
        String chunkFolderPath = getChunkFolderPath(fileMd5);
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_videofiles)
                        .object(chunkFolderPath + i)
                        .build())
                .collect(Collectors.toList());
        String filename = uploadFileParamsDto.getFilename();
        String extensionName = filename.substring(filename.lastIndexOf("."));
        String mergeFilePath = getFilePathByMd5(fileMd5, extensionName);
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_videofiles)
                .object(mergeFilePath)
                .sources(sourceObjectList)
                .build();
        try {
            ObjectWriteResponse response = minioClient.composeObject(composeObjectArgs);
            log.info("合并文件成功:{}", mergeFilePath);
        } catch (Exception e) {
            log.debug("合并文件失败，fileMd5：{}，异常：{}", fileMd5, e.getMessage(), e);
            return RestResponse.validfail(false, "合并文件失败");
        }
        log.info("文件合并完成，fileMd5：{}，mergeFilePath：{}", fileMd5, mergeFilePath);

        // 验证 MD5
        File minioFile = downloadFileFromMinIO(bucket_videofiles, mergeFilePath);
        if (minioFile == null) {
            log.debug("下载合并后文件失败，mergeFilePath：{}", mergeFilePath);
            return RestResponse.validfail(false, "下载合并后文件失败");
        }

        try (InputStream newFileInputStream = new FileInputStream(minioFile)) {
            String md5Hex = DigestUtils.md5Hex(newFileInputStream);
            if (!fileMd5.equals(md5Hex))
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败");
        } catch (Exception e) {
            log.debug("校验文件失败，fileMd5：{}，异常：{}", fileMd5, e.getMessage(), e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败");
        }
        uploadFileParamsDto.setFileSize(minioFile.length());
        log.info("文件校验成功，fileMd5：{}，mergeFilePath：{}", fileMd5, mergeFilePath);

        // 文件入库
        MediaFiles mediaFile = currentProxy.addMediaFilesToDB(companyId, fileMd5, uploadFileParamsDto, bucket_videofiles, mergeFilePath);
        if (mediaFile == null)
            return RestResponse.validfail(false, "文件入库失败");
        log.info("文件入库成功，fileMd5：{}，mergeFilePath：{}", fileMd5, mergeFilePath);

        // 清理分块文件
        clearChunkFiles(chunkFolderPath, chunkTotal);
        log.info("文件分块清理成功，chunkFolderPath：{}", chunkFolderPath);

        return RestResponse.success(true);
    }

    private String getFilePathByMd5(String fileMd5, String extensionName) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + extensionName;
    }

    @Override
    public File downloadFileFromMinIO(String bucket, String objectName) {
        // 临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            // 创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void clearChunkFiles(String chunkFileFolderPath, int chunkTocal) {
        Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i)
                .limit(chunkTocal)
                .map(i -> new DeleteObject(chunkFileFolderPath + i))
                .collect(Collectors.toList());
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucket_videofiles)
                .objects(objects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(item -> {
            try {
                DeleteError deleteError = item.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public MediaFiles getFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }
}
