package com.hrai.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrai.org.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @param tenantId 租户 ID
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND tenant_id = #{tenantId} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username, @Param("tenantId") String tenantId);

    /**
     * 根据邮箱查询用户
     *
     * @param email    邮箱
     * @param tenantId 租户 ID
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND tenant_id = #{tenantId} AND deleted = 0")
    SysUser selectByEmail(@Param("email") String email, @Param("tenantId") String tenantId);

    /**
     * 根据手机号查询用户
     *
     * @param phone    手机号
     * @param tenantId 租户 ID
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND tenant_id = #{tenantId} AND deleted = 0")
    SysUser selectByPhone(@Param("phone") String phone, @Param("tenantId") String tenantId);
}
