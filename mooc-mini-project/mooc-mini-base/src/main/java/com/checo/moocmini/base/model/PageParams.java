package com.checo.moocmini.base.model;


import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 分页查询通用参数
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PageParams {

    // 当前页码
    @ApiModelProperty("当前页码")
    private Long pageNo = 1L;

    // 每页记录数
    @ApiModelProperty("每页记录数")
    private Long pageSize = 30L;
}
