package com.checo.moocmini.learning;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableSwagger2Doc
@SpringBootApplication(scanBasePackages = "com.checo.moocmini")
@EnableFeignClients(basePackages = "com.checo.moocmini.learning.feignclient")
public class LearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningApplication.class, args);
    }
}
