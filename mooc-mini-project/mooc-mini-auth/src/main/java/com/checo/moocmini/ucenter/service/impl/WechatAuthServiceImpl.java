package com.checo.moocmini.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.checo.moocmini.ucenter.mapper.MoocminiUserMapper;
import com.checo.moocmini.ucenter.mapper.MoocminiUserRoleMapper;
import com.checo.moocmini.ucenter.model.dto.AuthParamsDto;
import com.checo.moocmini.ucenter.model.dto.MoocminiUserExt;
import com.checo.moocmini.ucenter.model.po.MoocminiUser;
import com.checo.moocmini.ucenter.model.po.MoocminiUserRole;
import com.checo.moocmini.ucenter.service.AuthService;
import com.checo.moocmini.ucenter.service.WechatAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


/**
 * 微信扫码认证
 */
@Slf4j
@Service("wechatAuthService")
public class WechatAuthServiceImpl implements AuthService, WechatAuthService {

    @Autowired
    private MoocminiUserMapper moocminiUserMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.secret}")
    private String secret;

    @Autowired
    private MoocminiUserRoleMapper moocminiUserRoleMapper;

    @Autowired
    private WechatAuthServiceImpl currentProxy;

    @Override
    public MoocminiUserExt execute(AuthParamsDto authParamsDto) {
        String username = authParamsDto.getUsername();
        MoocminiUser moocminiUser = moocminiUserMapper.selectOne(new LambdaQueryWrapper<MoocminiUser>().eq(MoocminiUser::getUsername, username));
        MoocminiUserExt moocminiUserExt = new MoocminiUserExt();
        BeanUtils.copyProperties(moocminiUser, moocminiUserExt);
        return moocminiUserExt;
    }

    @Override
    public MoocminiUser wechatAuth(String code) {
        // 申请令牌
        Map<String, String> accessTokenMap = getAccessToken(code);
        String accessToken = accessTokenMap.get("access_token");
        String openid = accessTokenMap.get("openid");

        // 携带令牌查询用户信息
        Map<String, String> userinfoMap = getUserinfo(accessToken, openid);
        if (userinfoMap == null)
            return null;

        // 保存用户信息到数据库
        MoocminiUser moocminiUser = currentProxy.addWechatUser(userinfoMap);

        // 返回
        return moocminiUser;
    }

    /**
     * 携带授权码申请令牌
     *
     * @param code 授权码
     * @return 响应实例：{
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    private Map<String, String> getAccessToken(String code) {

        String wechatUrlTemplate = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        String wechatUrl = String.format(wechatUrlTemplate, appid, secret, code);
        log.info("调用微信接口申请 accessToken, url：{}", wechatUrl);
        // 远程调用微信 Url
        ResponseEntity<String> exchange = restTemplate.exchange(wechatUrl, HttpMethod.POST, null, String.class);
        // 获取相应结果
        String result = exchange.getBody();
        log.info("调用微信接口申请 accessToken: 返回值：{}", result);
        Map<String, String> resultMap = JSON.parseObject(result, Map.class);
        return resultMap;
    }

    /**
     * 携带令牌获取用户信息
     */
    private Map<String, String> getUserinfo(String access_token, String openid) {

        String wechatUrlTemplate = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        //请求微信地址
        String wechatUrl = String.format(wechatUrlTemplate, access_token, openid);
        log.info("调用微信接口申请用户信息, url：{}", wechatUrl);
        ResponseEntity<String> exchange = restTemplate.exchange(wechatUrl, HttpMethod.POST, null, String.class);

        //防止乱码进行转码
        String result = new String(Objects.requireNonNull(exchange.getBody()).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        log.info("调用微信接口申请 accessToken: 返回值：{}", result);
        Map<String, String> resultMap = JSON.parseObject(result, Map.class);
        return resultMap;
    }


    /**
     * 保存用户信息
     */
    @Transactional
    public MoocminiUser addWechatUser(Map<String, String> userInfoMap) {
        String unionid = userInfoMap.get("unionid");
        // 根据 unionid 查询数据库
        MoocminiUser moocminiUser = moocminiUserMapper.selectOne(new LambdaQueryWrapper<MoocminiUser>().eq(MoocminiUser::getWxUnionid, unionid));
        if (moocminiUser != null)
            return moocminiUser;
        String userId = UUID.randomUUID().toString();
        moocminiUser = new MoocminiUser();
        moocminiUser.setId(userId);
        moocminiUser.setWxUnionid(unionid);
        // 记录从微信得到的昵称
        moocminiUser.setNickname(userInfoMap.get("nickname"));
        moocminiUser.setUserpic(userInfoMap.get("headimgurl"));
        moocminiUser.setName(userInfoMap.get("nickname"));
        moocminiUser.setUsername(unionid);
        moocminiUser.setPassword(unionid);
        moocminiUser.setUtype("101001"); // 学生类型
        moocminiUser.setStatus("1"); // 用户状态
        moocminiUser.setCreateTime(LocalDateTime.now());
        moocminiUserMapper.insert(moocminiUser);
        MoocminiUserRole moocminiUserRole = new MoocminiUserRole();
        moocminiUserRole.setId(UUID.randomUUID().toString());
        moocminiUserRole.setUserId(userId);
        moocminiUserRole.setRoleId("17"); // 学生角色
        moocminiUserRoleMapper.insert(moocminiUserRole);
        return moocminiUser;
    }


}
