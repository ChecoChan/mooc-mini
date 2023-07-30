package com.checo.moocmini.system.api;

import com.checo.moocmini.system.model.po.Dictionary;
import com.checo.moocmini.system.service.DictionaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@Api(value = "字典分类接口", tags = "字典分类接口")
@RestController
public class DictionaryController  {

    @Autowired
    private DictionaryService dictionaryService;

    @ApiOperation("查询所有字典分类")
    @GetMapping("/dictionary/all")
    public List<Dictionary> queryAll() {
        return dictionaryService.queryAll();
    }

    @ApiOperation("根据 Code 查询字典分类")
    @GetMapping("/dictionary/code/{code}")
    public Dictionary getByCode(@PathVariable String code) {
        return dictionaryService.getByCode(code);
    }
}
