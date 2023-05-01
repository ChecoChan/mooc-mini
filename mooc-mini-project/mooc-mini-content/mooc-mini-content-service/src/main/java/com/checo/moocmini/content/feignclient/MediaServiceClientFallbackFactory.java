package com.checo.moocmini.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile fileData, String objectName) throws IOException {
                // 降级方法
                log.debug("调用媒资管理服务上传文件时发生熔断，对象名：{}，异常信息：{}", objectName, throwable.toString(), throwable);
                return null;
            }
        };
    }
}

