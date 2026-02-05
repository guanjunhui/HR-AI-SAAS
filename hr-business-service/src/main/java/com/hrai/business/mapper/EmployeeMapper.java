package com.hrai.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrai.business.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工 Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
