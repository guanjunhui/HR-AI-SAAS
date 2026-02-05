package com.hrai.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrai.business.entity.EmployeeDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工详细信息 Mapper
 */
@Mapper
public interface EmployeeDetailMapper extends BaseMapper<EmployeeDetail> {
}
