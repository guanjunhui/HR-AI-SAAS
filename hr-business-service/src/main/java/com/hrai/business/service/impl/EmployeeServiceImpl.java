package com.hrai.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrai.business.dto.PageResponse;
import com.hrai.business.dto.employee.EmployeeCreateRequest;
import com.hrai.business.dto.employee.EmployeeDetailResponse;
import com.hrai.business.dto.employee.EmployeeQueryRequest;
import com.hrai.business.dto.employee.EmployeeUpdateRequest;
import com.hrai.business.entity.Employee;
import com.hrai.business.entity.Position;
import com.hrai.business.enums.EmployeeStatus;
import com.hrai.business.mapper.EmployeeMapper;
import com.hrai.business.mapper.PositionMapper;
import com.hrai.business.service.EmployeeService;
import com.hrai.common.constant.TenantConstants;
import com.hrai.common.context.TenantContext;
import com.hrai.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 员工服务实现
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final PositionMapper positionMapper;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper, PositionMapper positionMapper) {
        this.employeeMapper = employeeMapper;
        this.positionMapper = positionMapper;
    }

    @Override
    public PageResponse<EmployeeDetailResponse> list(EmployeeQueryRequest query) {
        String tenantId = resolveTenantId();
        Page<Employee> page = new Page<>(query.getPageNo(), query.getPageSize());

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getTenantId, tenantId);

        if (query.getOrgUnitId() != null) {
            wrapper.eq(Employee::getOrgUnitId, query.getOrgUnitId());
        }
        if (query.getPositionId() != null) {
            wrapper.eq(Employee::getPositionId, query.getPositionId());
        }
        if (StrUtil.isNotBlank(query.getEmployeeStatus())) {
            wrapper.eq(Employee::getEmployeeStatus, query.getEmployeeStatus());
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Employee::getEmployeeCode, keyword)
                    .or().like(Employee::getRealName, keyword)
                    .or().like(Employee::getPhone, keyword)
                    .or().like(Employee::getEmail, keyword));
        }
        wrapper.orderByDesc(Employee::getId);

        IPage<Employee> result = employeeMapper.selectPage(page, wrapper);

        // 批量加载岗位信息
        Map<Long, Position> positionMap = loadPositionMap(tenantId);
        // 批量加载上级信息
        Map<Long, Employee> managerMap = loadManagerMap(result.getRecords());

        List<EmployeeDetailResponse> records = result.getRecords().stream()
                .map(emp -> toDetail(emp, positionMap.get(emp.getPositionId()), managerMap.get(emp.getDirectManagerId())))
                .collect(Collectors.toList());

        return PageResponse.of(records, result.getTotal(), query.getPageNo(), query.getPageSize());
    }

    @Override
    public EmployeeDetailResponse getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null || !Objects.equals(employee.getTenantId(), resolveTenantId())) {
            throw new BizException(404, "员工不存在");
        }

        Position position = null;
        if (employee.getPositionId() != null) {
            position = positionMapper.selectById(employee.getPositionId());
        }

        Employee manager = null;
        if (employee.getDirectManagerId() != null) {
            manager = employeeMapper.selectById(employee.getDirectManagerId());
        }

        return toDetail(employee, position, manager);
    }

    @Override
    @Transactional
    public Long create(EmployeeCreateRequest request) {
        String tenantId = resolveTenantId();

        // 校验工号唯一性
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getTenantId, tenantId)
                .eq(Employee::getEmployeeCode, request.getEmployeeCode());
        if (employeeMapper.selectCount(wrapper) > 0) {
            throw new BizException(409, "工号已存在");
        }

        Employee employee = new Employee();
        employee.setTenantId(tenantId);
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setRealName(request.getRealName());
        employee.setGender(request.getGender());
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setIdCard(request.getIdCard());
        employee.setOrgUnitId(request.getOrgUnitId());
        employee.setPositionId(request.getPositionId());
        employee.setDirectManagerId(request.getDirectManagerId());
        employee.setWorkLocation(request.getWorkLocation());

        // 处理日期字段
        if (StrUtil.isNotBlank(request.getEntryDate())) {
            employee.setEntryDate(LocalDate.parse(request.getEntryDate(), DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (StrUtil.isNotBlank(request.getProbationEndDate())) {
            employee.setProbationEndDate(LocalDate.parse(request.getProbationEndDate(), DateTimeFormatter.ISO_LOCAL_DATE));
        }

        // 默认状态为试用期
        employee.setEmployeeStatus(EmployeeStatus.TRIAL.getCode());

        employeeMapper.insert(employee);
        return employee.getId();
    }

    @Override
    @Transactional
    public void update(Long id, EmployeeUpdateRequest request) {
        String tenantId = resolveTenantId();
        Employee employee = employeeMapper.selectById(id);
        if (employee == null || !Objects.equals(employee.getTenantId(), tenantId)) {
            throw new BizException(404, "员工不存在");
        }

        employee.setRealName(request.getRealName());
        employee.setGender(request.getGender());
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setIdCard(request.getIdCard());
        if (request.getOrgUnitId() != null) {
            employee.setOrgUnitId(request.getOrgUnitId());
        }
        if (request.getPositionId() != null) {
            employee.setPositionId(request.getPositionId());
        }
        employee.setDirectManagerId(request.getDirectManagerId());
        employee.setWorkLocation(request.getWorkLocation());

        employeeMapper.updateById(employee);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        String tenantId = resolveTenantId();
        Employee employee = employeeMapper.selectById(id);
        if (employee == null || !Objects.equals(employee.getTenantId(), tenantId)) {
            throw new BizException(404, "员工不存在");
        }

        // 使用带租户条件的删除
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getId, id).eq(Employee::getTenantId, tenantId);
        employeeMapper.delete(wrapper);
    }

    private EmployeeDetailResponse toDetail(Employee employee, Position position, Employee manager) {
        EmployeeDetailResponse resp = new EmployeeDetailResponse();
        resp.setId(employee.getId());
        resp.setEmployeeCode(employee.getEmployeeCode());
        resp.setRealName(employee.getRealName());
        resp.setGender(employee.getGender());
        resp.setPhone(employee.getPhone());
        resp.setEmail(employee.getEmail());
        resp.setOrgUnitId(employee.getOrgUnitId());
        resp.setPositionId(employee.getPositionId());
        if (position != null) {
            resp.setPositionName(position.getPositionName());
        }
        resp.setDirectManagerId(employee.getDirectManagerId());
        if (manager != null) {
            resp.setDirectManagerName(manager.getRealName());
        }
        resp.setEntryDate(employee.getEntryDate());
        resp.setProbationEndDate(employee.getProbationEndDate());
        resp.setRegularDate(employee.getRegularDate());
        resp.setResignationDate(employee.getResignationDate());
        resp.setEmployeeStatus(employee.getEmployeeStatus());

        // 设置状态描述
        EmployeeStatus status = EmployeeStatus.fromCode(employee.getEmployeeStatus());
        if (status != null) {
            resp.setEmployeeStatusDesc(status.getDesc());
        }

        resp.setWorkLocation(employee.getWorkLocation());
        resp.setCreatedAt(employee.getCreatedAt());
        resp.setUpdatedAt(employee.getUpdatedAt());
        return resp;
    }

    private Map<Long, Position> loadPositionMap(String tenantId) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Position::getTenantId, tenantId);
        List<Position> positions = positionMapper.selectList(wrapper);
        return positions.stream().collect(Collectors.toMap(Position::getId, p -> p, (a, b) -> a));
    }

    private Map<Long, Employee> loadManagerMap(List<Employee> employees) {
        List<Long> managerIds = employees.stream()
                .map(Employee::getDirectManagerId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (managerIds.isEmpty()) {
            return Map.of();
        }

        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Employee::getId, managerIds);
        List<Employee> managers = employeeMapper.selectList(wrapper);
        return managers.stream().collect(Collectors.toMap(Employee::getId, m -> m, (a, b) -> a));
    }

    private String resolveTenantId() {
        String tenantId = TenantContext.getTenantId();
        return tenantId == null || tenantId.isBlank() ? TenantConstants.DEFAULT_TENANT_ID : tenantId;
    }
}
