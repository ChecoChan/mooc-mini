package com.checo.moocmini.messagesdk.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * <P>
 * Mybatis-Plus 配置
 * </p>
 */
@Configuration("messagesdk_mpconfig")
@MapperScan("com.checo.moocmini.messagesdk.mapper")
public class MybatisPlusConfig {


}