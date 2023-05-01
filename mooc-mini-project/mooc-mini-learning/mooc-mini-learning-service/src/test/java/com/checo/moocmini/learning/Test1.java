package com.checo.moocmini.learning;

import com.checo.moocmini.learning.mapper.MoocminiChooseCourseMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class Test1 {

    @Autowired
    private MoocminiChooseCourseMapper moocminiChooseCourseMapper;

    @Test
    public void test() {

    }
}
