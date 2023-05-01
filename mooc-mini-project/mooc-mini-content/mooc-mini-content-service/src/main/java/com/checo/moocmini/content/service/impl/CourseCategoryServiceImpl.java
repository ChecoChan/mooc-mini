package com.checo.moocmini.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.checo.moocmini.content.mapper.CourseCategoryMapper;
import com.checo.moocmini.content.model.dto.CourseCategoryTreeDto;
import com.checo.moocmini.content.model.po.CourseCategory;
import com.checo.moocmini.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 调用 Mapper 查询出分类信息
        List<CourseCategoryTreeDto> courseCategoryTreeDtoList = baseMapper.selectTreeNodes("1");
        // 先将 list 转成 map，key 就是结点的 id，value 就是 CourseCategoryDto 对象
        Map<String, CourseCategoryTreeDto> courseCategoryTreeDtoMap = courseCategoryTreeDtoList
                .stream()
                .filter(item -> !id.equals(item.getId())) // 排除根节点
                .collect(Collectors.toMap(CourseCategory::getId, item -> item, (key1, key2) -> key2));
        // 遍历 courseCategoryTreeDtos 将子结点放在父结点的 ChildrenTreeNodes 属性中
        List<CourseCategoryTreeDto> result = new ArrayList<>();
        courseCategoryTreeDtoList
                .stream()
                .filter(item -> !id.equals(item.getId())) // 排除根结点
                .forEach(item -> {
                    if (item.getParentid().equals(id))
                        result.add(item);
                    // 找到子结点的父结点
                    CourseCategoryTreeDto parentNode = courseCategoryTreeDtoMap.get(item.getParentid());
                    if (parentNode != null) {
                        if (parentNode.getChildrenTreeNodes() == null)
                            parentNode.setChildrenTreeNodes(new ArrayList<>());
                        parentNode.getChildrenTreeNodes().add(item);
                    }
                });

        return result;
    }
}
