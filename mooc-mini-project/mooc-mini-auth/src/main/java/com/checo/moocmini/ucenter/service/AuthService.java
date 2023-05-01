package com.checo.moocmini.ucenter.service;

import com.checo.moocmini.ucenter.model.dto.AuthParamsDto;
import com.checo.moocmini.ucenter.model.dto.MoocminiUserExt;

/**
 * 统一的认证接口
 */
public interface AuthService {

    /**
     * 认证方法
     *
     * @param authParamsDto 认证参数
     * @return com.checo.moocmini.ucenter.model.po.MoocminiUser 用户信息
     */
    MoocminiUserExt execute(AuthParamsDto authParamsDto);
}
