package com.checo.moocmini.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.checo.moocmini.ucenter.model.po.MoocminiMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Checo
 */
public interface MoocminiMenuMapper extends BaseMapper<MoocminiMenu> {
    @Select("SELECT	* " +
            "FROM moocmini_menu " +
            "WHERE id IN (SELECT menu_id " +
            "             FROM moocmini_permission " +
            "             WHERE role_id IN (SELECT role_id " +
            "                               FROM moocmini_user_role " +
            "                               WHERE user_id = #{userId}))")
    List<MoocminiMenu> selectPermissionByUserId(@Param("userId") String userId);
}
