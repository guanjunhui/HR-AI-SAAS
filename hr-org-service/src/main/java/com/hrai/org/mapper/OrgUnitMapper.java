package com.hrai.org.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrai.org.entity.OrgUnit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 组织单元 Mapper
 */
@Mapper
public interface OrgUnitMapper extends BaseMapper<OrgUnit> {

    /**
     * 根据组织编码查询
     *
     * @param code     组织编码
     * @param tenantId 租户 ID
     * @return 组织单元
     */
    @Select("SELECT * FROM org_unit WHERE code = #{code} AND tenant_id = #{tenantId} AND deleted = 0")
    OrgUnit selectByCode(@Param("code") String code, @Param("tenantId") String tenantId);

    /**
     * 查询子组织 (直接下级)
     *
     * @param parentId 父级 ID
     * @param tenantId 租户 ID
     * @return 子组织列表
     */
    @Select("SELECT * FROM org_unit WHERE parent_id = #{parentId} AND tenant_id = #{tenantId} AND deleted = 0 ORDER BY sort_order")
    List<OrgUnit> selectChildren(@Param("parentId") Long parentId, @Param("tenantId") String tenantId);

    /**
     * 查询所有下级组织 (包括间接下级)
     *
     * @param path     组织路径
     * @param tenantId 租户 ID
     * @return 下级组织列表
     */
    @Select("SELECT * FROM org_unit WHERE path LIKE CONCAT(#{path}, '%') AND tenant_id = #{tenantId} AND deleted = 0 ORDER BY level, sort_order")
    List<OrgUnit> selectDescendants(@Param("path") String path, @Param("tenantId") String tenantId);
}
