package com.hrai.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrai.org.entity.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限 Mapper
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    @Select("SELECT permission_code FROM sys_role_permission WHERE tenant_id = #{tenantId} AND role_id = #{roleId}")
    List<String> selectCodesByRoleId(@Param("tenantId") String tenantId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM sys_role_permission WHERE tenant_id = #{tenantId} AND role_id = #{roleId}")
    int deleteByRoleId(@Param("tenantId") String tenantId, @Param("roleId") Long roleId);
}
