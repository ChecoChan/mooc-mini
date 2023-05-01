package com.checo.moocmini.content.model.dto;

import com.checo.moocmini.content.model.po.Teachplan;
import com.checo.moocmini.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

@Data
public class TeachplanDto extends Teachplan {

    private TeachplanMedia teachplanMedia;

    private List<TeachplanDto> teachPlanTreeNodes;
}
