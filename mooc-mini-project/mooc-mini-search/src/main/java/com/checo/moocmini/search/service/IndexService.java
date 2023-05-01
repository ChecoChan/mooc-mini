package com.checo.moocmini.search.service;


/**
 * 课程索引 service
 */
public interface IndexService {

    /**
     * 添加索引
     *
     * @param indexName 索引名称
     * @param id        主键
     * @param object    索引对象
     * @return true 表示成功，false失败
     */
    Boolean addCourseIndex(String indexName, String id, Object object);


    /**
     * 更新索引
     *
     * @param indexName 索引名称
     * @param id        主键
     * @param object    索引对象
     * @return Boolean true表示成功,false失败
     */
    Boolean updateCourseIndex(String indexName, String id, Object object);

    /**
     * 删除索引
     *
     * @param indexName 索引名称
     * @param id        主键
     * @return java.lang.Boolean
     */
    Boolean deleteCourseIndex(String indexName, String id);

}
