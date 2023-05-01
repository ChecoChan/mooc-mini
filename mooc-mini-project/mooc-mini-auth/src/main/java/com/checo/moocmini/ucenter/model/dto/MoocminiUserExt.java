package com.checo.moocmini.ucenter.model.dto;

import com.checo.moocmini.ucenter.model.po.MoocminiUser;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户扩展信息
 */
@Data
public class MoocminiUserExt extends MoocminiUser {
    //用户权限
    List<String> permissions = new ArrayList<>();
}
