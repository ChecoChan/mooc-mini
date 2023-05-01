package com.checo.moocmini.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.checo.moocmini.system.mapper.DictionaryMapper;
import com.checo.moocmini.system.model.po.Dictionary;
import com.checo.moocmini.system.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements DictionaryService {

    @Override
    public List<Dictionary> queryAll() {
        return this.list();
    }

    @Override
    public Dictionary getByCode(String code) {
        LambdaQueryWrapper<Dictionary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dictionary::getCode, code);
        return this.getOne(queryWrapper);
    }
}
