# Phase 1: 基础业务模块

> 组织与权限（M01）+ Core HR（M02）

## 概述

Phase 1 实现系统的基础业务模块，包括组织架构管理、用户权限管理、岗位管理、编制管理、员工档案和人事事件处理。

---

## 1. 数据库表设计（11张表）

### 1.1 组织与权限模块（M01）- 6张表

#### 1.1.1 组织单元表 (org_units)

```sql
-- 组织单元表
CREATE TABLE org_units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID，0表示顶级',
    unit_code VARCHAR(50) NOT NULL COMMENT '组织编码',
    unit_name VARCHAR(100) NOT NULL COMMENT '组织名称',
    unit_type VARCHAR(20) COMMENT '类型：company-公司/dept-部门/team-团队',
    manager_id BIGINT COMMENT '负责人ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-正常/1-删除',
    INDEX idx_tenant_parent (tenant_id, parent_id),
    UNIQUE KEY uk_tenant_code (tenant_id, unit_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织单元表';
```

**字段说明**：
- `parent_id`: 支持无限级组织树结构
- `unit_type`: 区分不同层级类型，便于UI展示
- `manager_id`: 关联 employees 表

#### 1.1.2 系统用户表 (sys_users)

```sql
-- 系统用户表
CREATE TABLE sys_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    user_code VARCHAR(50) NOT NULL COMMENT '工号',
    username VARCHAR(50) NOT NULL COMMENT '登录名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    org_unit_id BIGINT COMMENT '所属组织ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    last_login_at TIMESTAMP COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_username (tenant_id, username),
    INDEX idx_org_unit (org_unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';
```

**字段说明**：
- `password`: 使用 BCrypt 加密存储
- `user_code`: 与员工工号一致

#### 1.1.3 角色表 (sys_roles)

```sql
-- 角色表
CREATE TABLE sys_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description TEXT COMMENT '角色描述',
    data_scope TINYINT DEFAULT 1 COMMENT '数据范围：1-全部/2-本部门/3-本人',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

**预置角色**：
- `super_admin`: 超级管理员，data_scope=1
- `hr_admin`: HR管理员，data_scope=1
- `dept_manager`: 部门经理，data_scope=2
- `employee`: 普通员工，data_scope=3

#### 1.1.4 用户角色关联表 (sys_user_roles)

```sql
-- 用户角色关联表
CREATE TABLE sys_user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

#### 1.1.5 角色权限表 (sys_role_permissions)

```sql
-- 角色权限表
CREATE TABLE sys_role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role (role_id),
    UNIQUE KEY uk_role_permission (role_id, permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限表';
```

**权限编码规范**：
- 格式：`模块:资源:操作`
- 示例：
  - `org:unit:read` - 查看组织
  - `org:unit:write` - 编辑组织
  - `hr:employee:read` - 查看员工
  - `hr:employee:write` - 编辑员工
  - `hr:event:approve` - 审批人事事件

#### 1.1.6 审计日志表 (audit_logs)

```sql
-- 审计日志表
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    user_id BIGINT COMMENT '操作用户ID',
    user_name VARCHAR(50) COMMENT '操作用户名',
    module VARCHAR(50) COMMENT '模块名',
    action VARCHAR(50) COMMENT '操作类型',
    target_type VARCHAR(50) COMMENT '目标类型',
    target_id VARCHAR(64) COMMENT '目标ID',
    before_data JSON COMMENT '操作前数据',
    after_data JSON COMMENT '操作后数据',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '用户代理',
    result TINYINT COMMENT '结果：1-成功/0-失败',
    error_msg TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant_time (tenant_id, created_at),
    INDEX idx_user (user_id),
    INDEX idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
```

**操作类型枚举**：
- `CREATE`: 创建
- `UPDATE`: 更新
- `DELETE`: 删除
- `LOGIN`: 登录
- `LOGOUT`: 登出
- `APPROVE`: 审批
- `REJECT`: 驳回
- `EXPORT`: 导出

---

### 1.2 Core HR 模块（M02）- 5张表

#### 1.2.1 岗位表 (positions)

```sql
-- 岗位表
CREATE TABLE positions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    position_code VARCHAR(50) NOT NULL COMMENT '岗位编码',
    position_name VARCHAR(100) NOT NULL COMMENT '岗位名称',
    position_level INT COMMENT '职级',
    job_family VARCHAR(50) COMMENT '职族：tech-技术/sales-销售/support-支持/management-管理',
    org_unit_id BIGINT COMMENT '所属部门ID',
    description TEXT COMMENT '岗位描述',
    requirements TEXT COMMENT '任职要求',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用/0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, position_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';
```

#### 1.2.2 编制配置表 (headcounts)

```sql
-- 编制配置表
CREATE TABLE headcounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    org_unit_id BIGINT NOT NULL COMMENT '部门ID',
    position_id BIGINT NOT NULL COMMENT '岗位ID',
    budget_count INT NOT NULL COMMENT '编制数',
    current_count INT DEFAULT 0 COMMENT '在编人数',
    effective_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '失效日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_org_position (org_unit_id, position_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编制配置表';
```

**业务规则**：
- `current_count` 应自动维护，随员工入职/离职更新
- 当 `current_count >= budget_count` 时，提示编制已满

#### 1.2.3 员工表 (employees)

```sql
-- 员工表
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    employee_code VARCHAR(50) NOT NULL COMMENT '工号',
    sys_user_id BIGINT COMMENT '关联系统用户ID',
    real_name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT COMMENT '性别：1-男/2-女',
    birth_date DATE COMMENT '出生日期',
    id_card VARCHAR(20) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    org_unit_id BIGINT COMMENT '所属部门ID',
    position_id BIGINT COMMENT '岗位ID',
    direct_manager_id BIGINT COMMENT '直属上级ID',
    entry_date DATE COMMENT '入职日期',
    regularization_date DATE COMMENT '转正日期',
    contract_start DATE COMMENT '合同开始日期',
    contract_end DATE COMMENT '合同结束日期',
    employee_status VARCHAR(20) DEFAULT 'trial' COMMENT '状态：trial-试用期/regular-正式/resigned-离职',
    work_location VARCHAR(100) COMMENT '工作地点',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_code (tenant_id, employee_code),
    INDEX idx_org_unit (org_unit_id),
    INDEX idx_status (employee_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';
```

**员工状态枚举**：
- `trial`: 试用期
- `regular`: 正式员工
- `resigned`: 已离职

#### 1.2.4 员工详细信息表 (employee_details)

```sql
-- 员工详细信息表
CREATE TABLE employee_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    highest_education VARCHAR(20) COMMENT '最高学历：high_school/bachelor/master/doctor',
    graduate_school VARCHAR(100) COMMENT '毕业院校',
    major VARCHAR(100) COMMENT '专业',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系人电话',
    emergency_relation VARCHAR(20) COMMENT '与紧急联系人关系',
    bank_name VARCHAR(50) COMMENT '开户银行',
    bank_account VARCHAR(50) COMMENT '银行账号',
    current_address TEXT COMMENT '现居住地址',
    household_address TEXT COMMENT '户籍地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_employee (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工详细信息表';
```

#### 1.2.5 人事事件表 (employment_events)

```sql
-- 人事事件表（入职/转正/调岗/调薪/离职）
CREATE TABLE employment_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    event_code VARCHAR(64) NOT NULL UNIQUE COMMENT '事件编号',
    event_type VARCHAR(20) NOT NULL COMMENT '类型：onboard-入职/regularize-转正/transfer-调岗/salary_change-调薪/resign-离职',
    employee_id BIGINT COMMENT '员工ID(入职时为空)',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft-草稿/pending-审批中/approved-已通过/rejected-已驳回/withdrawn-已撤回',
    applicant_id BIGINT COMMENT '申请人ID',
    apply_date DATE COMMENT '申请日期',
    effective_date DATE COMMENT '生效日期',
    change_content JSON COMMENT '变更内容(JSON)',
    approver_ids JSON COMMENT '审批人列表(JSON数组)',
    current_approver_id BIGINT COMMENT '当前审批人ID',
    approve_records JSON COMMENT '审批记录(JSON数组)',
    attachments JSON COMMENT '附件列表(JSON数组)',
    remark TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_type (tenant_id, event_type),
    INDEX idx_employee (employee_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人事事件表';
```

**事件类型说明**：

| 类型 | 说明 | change_content 示例 |
|------|------|---------------------|
| onboard | 入职 | `{"name":"张三","position_id":1,"org_unit_id":2,"entry_date":"2025-02-01"}` |
| regularize | 转正 | `{"regularization_date":"2025-05-01","salary_adjustment":1000}` |
| transfer | 调岗 | `{"from_org_unit_id":1,"to_org_unit_id":2,"from_position_id":1,"to_position_id":3}` |
| salary_change | 调薪 | `{"before_salary":10000,"after_salary":12000,"reason":"年度调薪"}` |
| resign | 离职 | `{"resign_date":"2025-06-30","resign_type":"voluntary","handover_to_id":5}` |

---

## 2. 后端包结构

```
com.hrai.agent/
├── org/                              # M01 组织与权限
│   ├── controller/
│   │   ├── OrgUnitController.java    # 组织管理接口
│   │   ├── UserController.java       # 用户管理接口
│   │   ├── RoleController.java       # 角色管理接口
│   │   └── AuditLogController.java   # 审计日志接口
│   ├── service/
│   │   ├── OrgUnitService.java
│   │   ├── impl/
│   │   │   └── OrgUnitServiceImpl.java
│   │   ├── UserService.java
│   │   ├── impl/
│   │   │   └── UserServiceImpl.java
│   │   ├── RoleService.java
│   │   ├── impl/
│   │   │   └── RoleServiceImpl.java
│   │   └── AuditLogService.java
│   ├── mapper/
│   │   ├── OrgUnitMapper.java
│   │   ├── SysUserMapper.java
│   │   ├── SysRoleMapper.java
│   │   ├── SysUserRoleMapper.java
│   │   ├── SysRolePermissionMapper.java
│   │   └── AuditLogMapper.java
│   ├── entity/
│   │   ├── OrgUnit.java
│   │   ├── SysUser.java
│   │   ├── SysRole.java
│   │   ├── SysUserRole.java
│   │   ├── SysRolePermission.java
│   │   └── AuditLog.java
│   └── dto/
│       ├── OrgUnitDTO.java
│       ├── OrgTreeDTO.java           # 组织树DTO
│       ├── UserDTO.java
│       ├── UserCreateDTO.java
│       ├── RoleDTO.java
│       └── AuditLogQueryDTO.java
│
├── hr/                               # M02 Core HR
│   ├── controller/
│   │   ├── PositionController.java   # 岗位管理接口
│   │   ├── HeadcountController.java  # 编制管理接口
│   │   ├── EmployeeController.java   # 员工管理接口
│   │   └── EmploymentEventController.java  # 人事事件接口
│   ├── service/
│   │   ├── PositionService.java
│   │   ├── HeadcountService.java
│   │   ├── EmployeeService.java
│   │   └── EmploymentEventService.java
│   ├── statemachine/
│   │   ├── EmploymentEventStateMachine.java    # 人事事件状态机
│   │   ├── EmploymentEventState.java           # 状态枚举
│   │   └── EmploymentEventEvent.java           # 事件枚举
│   ├── mapper/
│   │   ├── PositionMapper.java
│   │   ├── HeadcountMapper.java
│   │   ├── EmployeeMapper.java
│   │   ├── EmployeeDetailMapper.java
│   │   └── EmploymentEventMapper.java
│   ├── entity/
│   │   ├── Position.java
│   │   ├── Headcount.java
│   │   ├── Employee.java
│   │   ├── EmployeeDetail.java
│   │   └── EmploymentEvent.java
│   └── dto/
│       ├── PositionDTO.java
│       ├── HeadcountDTO.java
│       ├── EmployeeDTO.java
│       ├── EmployeeCreateDTO.java
│       ├── EmployeeDetailDTO.java
│       ├── EmploymentEventDTO.java
│       └── EmploymentEventCreateDTO.java
```

---

## 3. 状态机设计

### 3.1 人事事件状态机 (EmploymentEventStateMachine)

```
状态流转：
                    ┌─────────────┐
                    │   draft     │
                    │  (草稿)     │
                    └──────┬──────┘
                           │ SUBMIT (提交)
                           ▼
                    ┌─────────────┐
              ┌─────│  pending    │─────┐
              │     │ (审批中)    │     │
              │     └──────┬──────┘     │
    REJECT    │            │ APPROVE    │ WITHDRAW
   (驳回)     ▼            ▼ (通过)     ▼ (撤回)
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │ rejected │ │ approved │ │withdrawn │
        │ (已驳回) │ │ (已通过) │ │(已撤回)  │
        └──────────┘ └──────────┘ └──────────┘
```

**事件定义**：
- `SUBMIT`: 提交审批
- `APPROVE`: 审批通过
- `REJECT`: 审批驳回
- `WITHDRAW`: 撤回申请

**实现要点**：
- 使用 Spring State Machine
- 每次状态变更记录到 `approve_records`
- 审批通过后触发对应的业务逻辑（如入职创建员工、转正更新状态等）

---

## 4. API 接口设计

### 4.1 组织管理接口 (OrgUnitController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/org/units/tree | 获取组织树 |
| GET | /api/org/units/{id} | 获取组织详情 |
| POST | /api/org/units | 创建组织 |
| PUT | /api/org/units/{id} | 更新组织 |
| DELETE | /api/org/units/{id} | 删除组织 |

### 4.2 用户管理接口 (UserController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/org/users | 用户列表（分页） |
| GET | /api/org/users/{id} | 用户详情 |
| POST | /api/org/users | 创建用户 |
| PUT | /api/org/users/{id} | 更新用户 |
| DELETE | /api/org/users/{id} | 删除用户 |
| PUT | /api/org/users/{id}/roles | 分配角色 |
| PUT | /api/org/users/{id}/password | 修改密码 |
| PUT | /api/org/users/{id}/status | 启用/禁用 |

### 4.3 角色管理接口 (RoleController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/org/roles | 角色列表 |
| GET | /api/org/roles/{id} | 角色详情 |
| POST | /api/org/roles | 创建角色 |
| PUT | /api/org/roles/{id} | 更新角色 |
| DELETE | /api/org/roles/{id} | 删除角色 |
| PUT | /api/org/roles/{id}/permissions | 分配权限 |

### 4.4 审计日志接口 (AuditLogController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/org/audit-logs | 审计日志列表（分页） |
| GET | /api/org/audit-logs/{id} | 审计日志详情 |

### 4.5 岗位管理接口 (PositionController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/hr/positions | 岗位列表 |
| GET | /api/hr/positions/{id} | 岗位详情 |
| POST | /api/hr/positions | 创建岗位 |
| PUT | /api/hr/positions/{id} | 更新岗位 |
| DELETE | /api/hr/positions/{id} | 删除岗位 |

### 4.6 编制管理接口 (HeadcountController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/hr/headcounts | 编制列表 |
| GET | /api/hr/headcounts/summary | 编制汇总（按部门） |
| POST | /api/hr/headcounts | 创建编制 |
| PUT | /api/hr/headcounts/{id} | 更新编制 |
| DELETE | /api/hr/headcounts/{id} | 删除编制 |

### 4.7 员工管理接口 (EmployeeController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/hr/employees | 员工列表（花名册） |
| GET | /api/hr/employees/{id} | 员工详情 |
| GET | /api/hr/employees/{id}/detail | 员工完整档案 |
| POST | /api/hr/employees | 创建员工 |
| PUT | /api/hr/employees/{id} | 更新员工 |
| PUT | /api/hr/employees/{id}/detail | 更新员工详情 |
| DELETE | /api/hr/employees/{id} | 删除员工 |
| POST | /api/hr/employees/import | 批量导入 |
| GET | /api/hr/employees/export | 导出Excel |

### 4.8 人事事件接口 (EmploymentEventController)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/hr/events | 事件列表 |
| GET | /api/hr/events/{id} | 事件详情 |
| POST | /api/hr/events | 创建事件 |
| PUT | /api/hr/events/{id} | 更新事件 |
| POST | /api/hr/events/{id}/submit | 提交审批 |
| POST | /api/hr/events/{id}/approve | 审批通过 |
| POST | /api/hr/events/{id}/reject | 审批驳回 |
| POST | /api/hr/events/{id}/withdraw | 撤回申请 |

---

## 5. 前端页面设计（20个）

### 5.1 组织与权限模块（5个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0001 | 组织列表 | /org/units | 树形展示，支持展开/折叠 |
| RQ0002 | 组织详情 | /org/units/:id | 查看/编辑组织信息 |
| RQ0003 | 用户列表 | /org/users | 表格展示，支持筛选 |
| RQ0004 | 用户详情 | /org/users/:id | 查看/编辑用户信息 |
| RQ0005 | 审计日志 | /org/audit-logs | 查看系统操作日志 |

**角色管理**（2个页面，合并显示）：
- 角色列表 + 角色详情（抽屉形式）

### 5.2 Core HR 模块（15个页面）

| 需求ID | 页面 | 路径 | 说明 |
|--------|------|------|------|
| RQ0006 | 岗位列表 | /hr/positions | 岗位管理列表 |
| RQ0007 | 岗位详情 | /hr/positions/:id | 查看/编辑岗位 |
| RQ0008 | 编制配置 | /hr/headcounts | 编制管理表格 |
| RQ0009 | 花名册 | /hr/employees | 员工列表 |
| RQ0010 | 员工档案 | /hr/employees/:id | 员工完整信息 |
| RQ0011 | 入职申请列表 | /hr/events/onboard | 入职事件列表 |
| RQ0012 | 入职申请表单 | /hr/events/onboard/create | 新建入职申请 |
| RQ0013 | 转正申请列表 | /hr/events/regularize | 转正事件列表 |
| RQ0014 | 转正申请表单 | /hr/events/regularize/create | 新建转正申请 |
| RQ0015 | 调岗申请列表 | /hr/events/transfer | 调岗事件列表 |
| RQ0016 | 调岗申请表单 | /hr/events/transfer/create | 新建调岗申请 |
| RQ0017 | 调薪申请列表 | /hr/events/salary-change | 调薪事件列表 |
| RQ0018 | 调薪申请表单 | /hr/events/salary-change/create | 新建调薪申请 |
| RQ0019 | 离职申请列表 | /hr/events/resign | 离职事件列表 |
| RQ0020 | 离职申请表单 | /hr/events/resign/create | 新建离职申请 |

---

## 6. 前端组件设计

### 6.1 公共组件

```
src/components/common/
├── OrgTreeSelect/                # 组织选择器
│   └── index.tsx
├── EmployeeSelect/               # 员工选择器
│   └── index.tsx
├── PositionSelect/               # 岗位选择器
│   └── index.tsx
├── ApprovalFlow/                 # 审批流程展示
│   └── index.tsx
├── StatusTag/                    # 状态标签
│   └── index.tsx
└── AuditTimeline/                # 审计时间线
    └── index.tsx
```

### 6.2 组件说明

**OrgTreeSelect（组织选择器）**：
- 支持树形下拉选择
- 支持搜索过滤
- 支持多选/单选
- Props: `value`, `onChange`, `multiple`, `excludeIds`

**ApprovalFlow（审批流程）**：
- 展示审批进度
- 显示每个审批节点的状态
- 支持点击查看审批意见

---

## 7. 验证标准

### 7.1 功能验证

- [ ] 组织树CRUD正常，支持无限级嵌套
- [ ] 用户创建后可登录系统
- [ ] 角色权限分配后生效
- [ ] 审计日志自动记录所有关键操作
- [ ] 岗位编制关联正确
- [ ] 员工档案信息完整
- [ ] 人事事件状态流转正确
- [ ] 入职审批通过后自动创建员工
- [ ] 离职审批通过后员工状态变更

### 7.2 数据验证

- [ ] 多租户数据隔离
- [ ] 工号唯一性校验
- [ ] 身份证格式校验
- [ ] 手机号格式校验

### 7.3 性能验证

- [ ] 组织树加载 < 500ms
- [ ] 员工列表分页 < 200ms
- [ ] 批量导入1000条 < 30s

---

## 8. 交付物清单

| 类型 | 数量 | 说明 |
|------|------|------|
| 数据库表 | 11张 | org_units, sys_users 等 |
| Entity | 11个 | 对应数据库表 |
| Mapper | 11个 | MyBatis Plus Mapper |
| Service | 8个 | 业务服务类 |
| Controller | 8个 | REST接口 |
| DTO | 15+ | 数据传输对象 |
| 前端页面 | 20个 | React 页面组件 |
| 公共组件 | 6个 | 可复用组件 |

---

*Phase 1 完成后，系统将具备基础的组织管理和人员管理能力，为后续业务模块奠定基础。*
