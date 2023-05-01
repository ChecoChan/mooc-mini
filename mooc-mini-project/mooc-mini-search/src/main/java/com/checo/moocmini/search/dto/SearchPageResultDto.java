package com.checo.moocmini.search.dto;

import com.checo.moocmini.base.model.PageResult;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SearchPageResultDto<T> extends PageResult {

    //大分类列表
    List<String> mtList;
    //小分类列表
    List<String> stList;

    public SearchPageResultDto(List<T> items, long counts, long page, long pageSize) {
        super(items, counts, page, pageSize);
    }

}
