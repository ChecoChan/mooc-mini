package com.checo.moocmini.content.feignclient;

import com.checo.moocmini.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 媒资管理服务远程接口
 */
@FeignClient(value = "media-api", configuration = MultipartSupportConfig.class, fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient {

    @PostMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String upload(@RequestPart("filedata") MultipartFile fileData,
                  @RequestParam(value = "objectName", required = false) String objectName) throws IOException;
}
