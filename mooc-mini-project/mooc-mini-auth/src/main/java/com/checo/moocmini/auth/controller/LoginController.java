package com.checo.moocmini.auth.controller;

import com.checo.moocmini.ucenter.mapper.MoocminiUserMapper;
import com.checo.moocmini.ucenter.model.po.MoocminiUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试controller
 */
@Slf4j
@RestController
public class LoginController {

    @Autowired
    private MoocminiUserMapper moocminiUserMapper;

    @RequestMapping("/login-success")
    public String loginSuccess() {

        return "登录成功";
    }

    @RequestMapping("/user/{id}")
    public MoocminiUser getuser(@PathVariable("id") String id) {
        MoocminiUser moocminiUser = moocminiUserMapper.selectById(id);
        return moocminiUser;
    }

    @RequestMapping("/r/r1")
    public String r1() {
        return "访问 r1 资源";
    }

    @RequestMapping("/r/r2")
    public String r2() {
        return "访问 r2 资源";
    }

}
