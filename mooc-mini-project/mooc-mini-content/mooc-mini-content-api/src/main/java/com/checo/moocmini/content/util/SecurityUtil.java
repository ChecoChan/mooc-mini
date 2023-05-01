package com.checo.moocmini.content.util;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
public class SecurityUtil {

    public static MoocminiUser getUser() {
        try {
            // 获取当前用户身份
            Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principalObj instanceof String) {
                // 取出用户身份信息
                String principal = principalObj.toString();
                // 将 Json 转成对象
                MoocminiUser user = JSON.parseObject(principal, MoocminiUser.class);
                return user;
            }
        } catch (Exception e) {
            log.error("获取当前登录用户身份出错：{}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Data
    public static class MoocminiUser implements Serializable {

        private static final long serialVersionUID = 1L;

        private String id;

        private String username;

        private String password;

        private String salt;

        private String name;
        private String nickname;
        private String wxUnionid;
        private String companyId;
        /**
         * 头像
         */
        private String userpic;

        private String utype;

        private LocalDateTime birthday;

        private String sex;

        private String email;

        private String cellphone;

        private String qq;

        /**
         * 用户状态
         */
        private String status;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;
    }
}
