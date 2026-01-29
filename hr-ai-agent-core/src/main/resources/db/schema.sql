-- ============================================
-- HR AI SaaS - Agent 核心服务数据表结构
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hr_ai_saas
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE hr_ai_saas;

-- ============================================
-- 1. Agent 编排相关表
-- ============================================

-- Agent 定义表
CREATE TABLE agents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    agent_type VARCHAR(50) NOT NULL COMMENT 'Agent类型 (hr_policy/recruiting/ticket_router)',
    agent_name VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    description TEXT COMMENT '描述',
    system_prompt TEXT NOT NULL COMMENT '系统提示词',
    model_name VARCHAR(50) NOT NULL DEFAULT 'qwen-plus' COMMENT '模型名称',
    temperature DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数',
    max_tokens INT DEFAULT 2000 COMMENT '最大Token数',
    tools_config JSON COMMENT '工具配置 (JSON数组)',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_type (tenant_id, agent_type),
    INDEX idx_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent定义表';

-- 工作流定义表
CREATE TABLE workflows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    workflow_name VARCHAR(100) NOT NULL COMMENT '工作流名称',
    description TEXT COMMENT '描述',
    dag_definition JSON NOT NULL COMMENT 'DAG定义 (节点和边)',
    trigger_conditions JSON COMMENT '触发条件',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant (tenant_id),
    INDEX idx_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流定义表';

-- 工作流执行记录表
CREATE TABLE workflow_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    workflow_id BIGINT NOT NULL COMMENT '工作流ID',
    execution_id VARCHAR(64) NOT NULL UNIQUE COMMENT '执行ID',
    status VARCHAR(20) NOT NULL COMMENT '状态 (running/completed/failed)',
    current_step VARCHAR(100) COMMENT '当前步骤',
    execution_context JSON COMMENT '执行上下文',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant_workflow (tenant_id, workflow_id),
    INDEX idx_execution_id (execution_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流执行记录表';

-- ============================================
-- 2. 记忆管理相关表
-- ============================================

-- 用户画像表
CREATE TABLE user_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    department VARCHAR(100) COMMENT '部门',
    position VARCHAR(100) COMMENT '职位',
    frequently_asked_topics JSON COMMENT '常问话题 (JSON数组)',
    preferences JSON COMMENT '用户偏好',
    total_questions INT DEFAULT 0 COMMENT '总提问数',
    avg_satisfaction DECIMAL(3,2) COMMENT '平均满意度',
    last_interaction_at TIMESTAMP COMMENT '最后交互时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_tenant_user (tenant_id, user_id),
    INDEX idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户画像表';

-- 对话历史表 (长期记忆)
CREATE TABLE conversation_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    question TEXT NOT NULL COMMENT '用户问题',
    answer TEXT COMMENT 'Agent回答',
    answer_summary TEXT COMMENT '答案摘要 (用于向量化)',
    agent_type VARCHAR(50) COMMENT '使用的Agent类型',
    cited_docs JSON COMMENT '引用的文档',
    satisfaction_score INT COMMENT '满意度评分 (1-5)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant_user (tenant_id, user_id),
    INDEX idx_session (session_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话历史表';

-- ============================================
-- 3. 工具调用相关表
-- ============================================

-- Function 定义表
CREATE TABLE functions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    function_name VARCHAR(100) NOT NULL UNIQUE COMMENT 'Function名称',
    description TEXT COMMENT '描述',
    parameters_schema JSON NOT NULL COMMENT '参数Schema (JSON Schema)',
    implementation_class VARCHAR(200) NOT NULL COMMENT '实现类全限定名',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Function定义表';

-- Function 调用日志表
CREATE TABLE function_invocations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    user_id VARCHAR(64) COMMENT '用户ID',
    session_id VARCHAR(64) COMMENT '会话ID',
    function_name VARCHAR(100) NOT NULL COMMENT 'Function名称',
    input_params JSON COMMENT '输入参数',
    output_result JSON COMMENT '输出结果',
    status VARCHAR(20) NOT NULL COMMENT '状态 (success/failed)',
    error_message TEXT COMMENT '错误信息',
    execution_time_ms INT COMMENT '执行时间(毫秒)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant_user (tenant_id, user_id),
    INDEX idx_function (function_name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Function调用日志表';

-- ============================================
-- 4. 知识库管理相关表
-- ============================================

-- 知识库文档表
CREATE TABLE knowledge_docs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    space_id VARCHAR(64) COMMENT '知识空间ID',
    doc_id VARCHAR(64) NOT NULL UNIQUE COMMENT '文档ID',
    doc_title VARCHAR(200) NOT NULL COMMENT '文档标题',
    doc_type VARCHAR(50) COMMENT '文档类型 (policy/procedure/faq)',
    category VARCHAR(100) COMMENT '分类',
    content LONGTEXT NOT NULL COMMENT '文档内容',
    metadata JSON COMMENT '元数据',
    allowed_roles JSON COMMENT '允许访问的角色',
    vector_indexed TINYINT(1) DEFAULT 0 COMMENT '是否已向量化',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_space (tenant_id, space_id),
    INDEX idx_category (category),
    INDEX idx_vector_indexed (vector_indexed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 文档分块表
CREATE TABLE knowledge_chunks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    doc_id VARCHAR(64) NOT NULL COMMENT '文档ID',
    chunk_id VARCHAR(64) NOT NULL UNIQUE COMMENT '分块ID',
    chunk_index INT NOT NULL COMMENT '分块序号',
    chunk_content TEXT NOT NULL COMMENT '分块内容',
    metadata JSON COMMENT '元数据',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_doc_id (doc_id),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分块表';

-- ============================================
-- 5. 租户与成本管理相关表
-- ============================================

-- 租户配置表
CREATE TABLE tenants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL UNIQUE COMMENT '租户ID',
    tenant_name VARCHAR(100) NOT NULL COMMENT '租户名称',
    plan_type VARCHAR(50) NOT NULL COMMENT '套餐类型 (free/pro/enterprise)',
    token_budget INT DEFAULT 0 COMMENT 'Token预算 (每月)',
    token_used INT DEFAULT 0 COMMENT '已使用Token',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户配置表';

-- Token 使用记录表
CREATE TABLE token_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    user_id VARCHAR(64) COMMENT '用户ID',
    session_id VARCHAR(64) COMMENT '会话ID',
    model_name VARCHAR(50) COMMENT '模型名称',
    prompt_tokens INT COMMENT '提示Token数',
    completion_tokens INT COMMENT '完成Token数',
    total_tokens INT COMMENT '总Token数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant_date (tenant_id, created_at),
    INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token使用记录表';
