package com.checo.moocmini.ucenter.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/checkcode")
@FeignClient(value = "checkcode", fallbackFactory = CheckCodeClientFactory.class)
public interface CheckCodeClient {

    @PostMapping("/verify")
    Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);
}
