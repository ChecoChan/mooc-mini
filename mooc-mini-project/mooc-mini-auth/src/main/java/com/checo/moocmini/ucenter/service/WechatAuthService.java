package com.checo.moocmini.ucenter.service;

import com.checo.moocmini.ucenter.model.po.MoocminiUser;

/**
 * 微信扫码接口
 */
public interface WechatAuthService {

    /**
     * 微信扫码认证：申请令牌，携带令牌查询用户信息，保存用户信息到数据库
     * @param code 授权码
     */
    MoocminiUser wechatAuth(String code);
}
