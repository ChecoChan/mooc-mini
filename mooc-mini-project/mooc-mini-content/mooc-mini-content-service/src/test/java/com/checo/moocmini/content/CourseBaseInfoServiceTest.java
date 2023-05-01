package com.checo.moocmini.content;


import com.checo.moocmini.base.model.PageParams;
import com.checo.moocmini.base.model.PageResult;
import com.checo.moocmini.content.model.dto.AddCourseDto;
import com.checo.moocmini.content.model.dto.CourseBaseInfoDto;
import com.checo.moocmini.content.model.dto.QueryCourseParamsDto;
import com.checo.moocmini.content.model.po.CourseBase;
import com.checo.moocmini.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CourseBaseInfoServiceTest {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testQueryCourseBaseList() {

        // 分页参数对象
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(3L);

        // 查询条件
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java"); // 课程名称包含 ”java“
        courseParamsDto.setAuditStatus("202004"); // 课程审核通过

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(null, pageParams, courseParamsDto);
        System.out.println(courseBasePageResult);
    }

    @Test
    public void testCreateCourseBase() {
        Long companyId = 1232141425L;

        AddCourseDto addCourseDto = new AddCourseDto();
        addCourseDto.setName("测试课程888");
        addCourseDto.setUsers("初级人员");
        addCourseDto.setTags("tags");
        addCourseDto.setMt("1-1");
        addCourseDto.setSt("1-1-1");
        addCourseDto.setGrade("204001");
        addCourseDto.setTeachmode("200002");
        addCourseDto.setDescription("测试课程888");
        addCourseDto.setPic("pic");
        addCourseDto.setCharge("201001");
        addCourseDto.setPrice(99f);
        addCourseDto.setOriginalPrice(100f);
        addCourseDto.setQq("22333");
        addCourseDto.setPhone("13000000000");
        addCourseDto.setValidDays(150);

        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        System.out.println(courseBaseInfoDto);
    }
}
