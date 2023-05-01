package com.checo.moocmini.learning;

import com.checo.moocmini.content.model.po.CoursePublish;
import com.checo.moocmini.learning.feignclient.ContentServiceClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
public class FeignClientTest {

    @Autowired
    private ContentServiceClient contentServiceClient;


    @Test
    public void testContentServiceClient() {
        CoursePublish coursepublish = contentServiceClient.getCoursePublish(2L);
        Assertions.assertNotNull(coursepublish);
    }
}