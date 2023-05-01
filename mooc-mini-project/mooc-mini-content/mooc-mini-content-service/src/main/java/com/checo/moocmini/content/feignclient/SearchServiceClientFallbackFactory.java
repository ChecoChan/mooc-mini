package com.checo.moocmini.content.feignclient;

import com.checo.moocmini.content.model.po.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {

    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                // 降级方法
                log.debug("调用课程索引服务发生熔断，课程索引信息：{}，异常信息：{}", courseIndex, throwable.toString(), throwable);
                return false;
            }
        };
    }
}
