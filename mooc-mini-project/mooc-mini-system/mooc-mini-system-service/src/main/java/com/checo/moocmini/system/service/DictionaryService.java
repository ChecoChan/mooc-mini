package com.checo.moocmini.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.checo.moocmini.system.model.po.Dictionary;

import java.util.List;

public interface DictionaryService extends IService<Dictionary> {

    /**
     * 查询所有数据字典内容
     * @return 查询结果
     */
    List<Dictionary> queryAll();

    /**
     * 根据 code 查询数据字典
     * @param code -- String 数据字典Code
     * @return 查询结果
     */
    Dictionary getByCode(String code);
}
