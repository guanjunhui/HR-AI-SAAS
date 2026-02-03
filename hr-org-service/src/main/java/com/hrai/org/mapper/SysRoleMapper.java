package com.hrai.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrai.org.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色 Mapper
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据角色编码查询角色
     *
     * @param code     角色编码
     * @param tenantId 租户 ID
     * @return 角色信息
     */
    @Select("SELECT * FROM sys_role WHERE code = #{code} AND tenant_id = #{tenantId} AND deleted = 0")
    SysRole selectByCode(@Param("code") String code, @Param("tenantId") String tenantId);
}
