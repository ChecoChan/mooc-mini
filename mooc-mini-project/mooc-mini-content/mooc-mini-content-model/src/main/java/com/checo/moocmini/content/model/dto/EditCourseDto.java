package com.checo.moocmini.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EditCourseDto extends AddCourseDto {

    @ApiModelProperty(value = "课程 id", required = true)
    private Long id;
}
