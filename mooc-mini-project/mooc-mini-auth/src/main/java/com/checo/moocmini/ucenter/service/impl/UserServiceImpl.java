package com.checo.moocmini.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.checo.moocmini.ucenter.mapper.MoocminiMenuMapper;
import com.checo.moocmini.ucenter.mapper.MoocminiUserMapper;
import com.checo.moocmini.ucenter.model.dto.AuthParamsDto;
import com.checo.moocmini.ucenter.model.dto.MoocminiUserExt;
import com.checo.moocmini.ucenter.model.po.MoocminiMenu;
import com.checo.moocmini.ucenter.model.po.MoocminiUser;
import com.checo.moocmini.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private MoocminiUserMapper moocminiUserMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MoocminiMenuMapper moocminiMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        // 将传入的 Json 转成 AuthParamsDto 对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求：{}", s);
            throw new RuntimeException("认证请求数据格式不符合要求");
        }

        // 获取认证类型
        String authType = authParamsDto.getAuthType();
        // 根据认证类型使用指定的认证服务
        String beanName = authType + "AuthService";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        // 调用统一的 execute 方法，完成认证
        MoocminiUserExt moocminiUserExt = authService.execute(authParamsDto);

        // 返回所需类型
        return getUserPrincipal(moocminiUserExt);
    }

    /**
     * 查询用户信息
     */
    private UserDetails getUserPrincipal(MoocminiUserExt moocminiUserExt) {
        String password = moocminiUserExt.getPassword();
        // 根据用户 id 查询用户权限用户权限
        String[] authorities = null;
        List<MoocminiMenu> menuList = moocminiMenuMapper.selectPermissionByUserId(moocminiUserExt.getId());
        if (menuList.size() > 0) {
            List<String> permissions = new ArrayList<>();
            menuList.forEach(item -> { permissions.add(item.getCode()); });
            authorities = permissions.toArray(new String[0]);
        }
        // 将用户信息(不包括密码)转成 Json
        moocminiUserExt.setPassword(null);
        String moocminiUserJson = JSON.toJSONString(moocminiUserExt);

        // 返回所需对象
        return User.withUsername(moocminiUserJson).password(password).authorities(authorities).build();
    }
}
