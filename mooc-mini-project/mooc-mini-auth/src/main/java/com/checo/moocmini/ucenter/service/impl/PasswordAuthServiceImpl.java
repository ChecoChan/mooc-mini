package com.checo.moocmini.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.checo.moocmini.ucenter.feignclient.CheckCodeClient;
import com.checo.moocmini.ucenter.mapper.MoocminiUserMapper;
import com.checo.moocmini.ucenter.model.dto.AuthParamsDto;
import com.checo.moocmini.ucenter.model.dto.MoocminiUserExt;
import com.checo.moocmini.ucenter.model.po.MoocminiUser;
import com.checo.moocmini.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 账号密码认证
 */
@Service("passwordAuthService")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    private MoocminiUserMapper moocminiUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CheckCodeClient checkCodeClient;

    @Override
    public MoocminiUserExt execute(AuthParamsDto authParamsDto) {

        // 账号
        String username = authParamsDto.getUsername();
        if (StringUtils.isEmpty(username))
            throw new RuntimeException("请输入账号");
        // 校验账号是否存在
        MoocminiUser moocminiUser = moocminiUserMapper.selectOne(new LambdaQueryWrapper<MoocminiUser>().eq(MoocminiUser::getUsername, username));
        if (moocminiUser == null)
            throw new RuntimeException("账号不存在");
        // 校验密码正确性
        String password = authParamsDto.getPassword();
        String realPassword = moocminiUser.getPassword();
        boolean matches = passwordEncoder.matches(password, realPassword);
        if (!matches)
            throw new RuntimeException("密码错误");
        // 校验验证码 - 远程调用验证码服务接口校验验证码
        String checkcodekey = authParamsDto.getCheckcodekey();
        String checkcode = authParamsDto.getCheckcode();
        if (StringUtils.isEmpty(checkcodekey) || StringUtils.isEmpty(checkcode))
            throw new RuntimeException("请输入验证码");
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (!verify)
            throw new RuntimeException("验证码错误");

        // 返回所需对象
        MoocminiUserExt moocminiUserExt = new MoocminiUserExt();
        BeanUtils.copyProperties(moocminiUser, moocminiUserExt);
        return moocminiUserExt;
    }
}
