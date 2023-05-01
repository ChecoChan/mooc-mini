package com.checo.moocmini.auth.controller;

import com.checo.moocmini.ucenter.model.po.MoocminiUser;
import com.checo.moocmini.ucenter.service.WechatAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Slf4j
@Controller
public class WechatLoginController {

    @Autowired
    private WechatAuthService wechatAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code：{}, state：{}", code, state);
        // 请求微信申请令牌
        MoocminiUser moocminiUser = wechatAuthService.wechatAuth(code);
        if (moocminiUser == null)
            return "redirect:http://www.mooc-mini.cn/error.html";

        String username = moocminiUser.getUsername();
        return "redirect:http://www.mooc-mini.cn/sign.html?username=" + username + "&authType=wechat";
    }
}
