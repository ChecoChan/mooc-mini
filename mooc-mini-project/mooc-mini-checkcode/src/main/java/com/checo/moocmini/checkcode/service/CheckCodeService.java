package com.checo.moocmini.checkcode.service;

import com.checo.moocmini.checkcode.model.CheckCodeParamsDto;
import com.checo.moocmini.checkcode.model.CheckCodeResultDto;

/**
 * 验证码接口
 */
public interface CheckCodeService {

    /**
     * 生成验证码
     *
     * @param checkCodeParamsDto 生成验证码参数
     * @return com.checo.moocmini.checkcode.model.CheckCodeResultDto 验证码结果
     */
    CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

    /**
     * 校验验证码
     */
    boolean verify(String key, String code);


    /**
     * 验证码生成器
     */
    interface CheckCodeGenerator {
        /**
         * 验证码生成
         *
         * @return 验证码
         */
        String generate(int length);


    }

    /**
     * key 生成器
     */
    interface KeyGenerator {

        /**
         * key 生成
         *
         * @return 验证码
         */
        String generate(String prefix);
    }


    /**
     * 验证码存储
     */
    interface CheckCodeStore {

        /**
         * 向缓存设置 key
         *
         * @param key    key
         * @param value  value
         * @param expire 过期时间,单位秒
         */
        void set(String key, String value, Integer expire);

        String get(String key);

        void remove(String key);
    }
}
