package com.checo.moocmini.content;

import com.checo.moocmini.content.mapper.CourseCategoryMapper;
import com.checo.moocmini.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryMapperTest {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Test
    public void testCourseCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
