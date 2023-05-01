package com.checo.moocmini.content;

import com.checo.moocmini.content.model.dto.CourseCategoryTreeDto;
import com.checo.moocmini.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryServiceTest {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    public void testCourseCategoryService() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
