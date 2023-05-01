package com.checo.moocmini.base.model;

import lombok.*;

import java.io.Serializable;
import java.util.List;


/**
 * 分页查询结果模型类
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    // 数据列表
    private List<T> items;
    // 总记录数
    private Long counts;
    // 当前页码
    private Long page;
    // 每页记录数
    private Long pageSize;
}
