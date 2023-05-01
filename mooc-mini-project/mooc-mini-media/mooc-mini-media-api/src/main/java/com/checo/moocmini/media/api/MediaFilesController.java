package com.checo.moocmini.media.api;

import com.checo.moocmini.base.model.PageParams;
import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.media.model.dto.QueryMediaParamsDto;
import com.checo.moocmini.media.model.dto.UploadFileParamsDto;
import com.checo.moocmini.media.model.dto.UploadFileResultDto;
import com.checo.moocmini.media.model.po.MediaFiles;
import com.checo.moocmini.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);
    }

    @ApiOperation("上传文件")
    @PostMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile fileData,
                                      @RequestParam(value = "objectName", required = false) String objectName) throws IOException {
        Long companyId = 1232141425L;

        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileData.getOriginalFilename());
        uploadFileParamsDto.setFileSize(uploadFileParamsDto.getFileSize());
        uploadFileParamsDto.setFileType("001001");

        File tempFile = File.createTempFile("minio", ".temp");
        fileData.transferTo(tempFile);
        String localFilePath = tempFile.getAbsolutePath();

        return mediaFileService.upload(companyId, uploadFileParamsDto, localFilePath, objectName);
    }
}
