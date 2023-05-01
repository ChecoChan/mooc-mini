package com.checo.moocmini.content;

import com.checo.moocmini.content.config.MultipartSupportConfig;
import com.checo.moocmini.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 测试使用 feign 远程上传文件
 */
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Test
    public void testFeignUpload() throws IOException {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("C:/IT/TestGarbage/2.html"));
        String upload = mediaServiceClient.upload(multipartFile, "course/2.html");
        if (upload == null)
            System.out.println("媒资服务熔断，走了降级逻辑");
    }
}
