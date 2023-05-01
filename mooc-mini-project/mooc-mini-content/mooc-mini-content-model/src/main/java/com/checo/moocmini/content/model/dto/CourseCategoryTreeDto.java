package com.checo.moocmini.content.model.dto;

import com.checo.moocmini.content.model.po.CourseCategory;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {

    List<CourseCategoryTreeDto> childrenTreeNodes;
}
