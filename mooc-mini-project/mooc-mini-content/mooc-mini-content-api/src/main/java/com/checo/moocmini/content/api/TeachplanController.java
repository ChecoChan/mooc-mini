package com.checo.moocmini.content.api;

import com.checo.moocmini.content.model.dto.BindTeachplanMediaDto;
import com.checo.moocmini.content.model.dto.SaveTeachplanDto;
import com.checo.moocmini.content.model.dto.TeachplanDto;
import com.checo.moocmini.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程计划管理接口", tags = "课程计划管理接口")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> queryTeachplanTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto) {
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    @ApiOperation("课程计划删除")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void deleteTeachplan(@PathVariable Long teachplanId) {
        teachplanService.deleteTeachplan(teachplanId);
    }

    @ApiOperation("课程计划排序 - 下移")
    @PostMapping("/teachplan/movedown/{teachplanId}")
    public void moveDown(@PathVariable Long teachplanId) {
        teachplanService.moveDown(teachplanId);
    }

    @ApiOperation("课程计划排序 - 上移")
    @PostMapping("/teachplan/moveup/{teachplanId}")
    public void moveUp(@PathVariable Long teachplanId) {
        teachplanService.moveUp(teachplanId);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

}
