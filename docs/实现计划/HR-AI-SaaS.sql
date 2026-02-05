create table if not exists agents
(
    id            bigint auto_increment comment 'ID'
        primary key,
    tenant_id     varchar(64)                             not null comment 'ç§Ÿæˆ·ID',
    agent_type    varchar(50)                             not null comment 'Agentç±»åž‹ (hr_policy/recruiting/ticket_router)',
    agent_name    varchar(100)                            not null comment 'Agentåç§°',
    description   text                                    null comment 'æè¿°',
    system_prompt text                                    not null comment 'ç³»ç»Ÿæç¤ºè¯',
    model_name    varchar(50)   default 'qwen-plus'       not null comment 'æ¨¡åž‹åç§°',
    temperature   decimal(3, 2) default 0.70              null comment 'æ¸©åº¦å‚æ•°',
    max_tokens    int           default 2000              null comment 'æœ€å¤§Tokenæ•°',
    tools_config  json                                    null comment 'å·¥å…·é…ç½® (JSONæ•°ç»„)',
    is_enabled    tinyint(1)    default 1                 null comment 'æ˜¯å¦å¯ç”¨',
    created_at    timestamp     default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´',
    updated_at    timestamp     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'æ›´æ–°æ—¶é—´',
    deleted       tinyint(1)    default 0                 null comment 'é€»è¾‘åˆ é™¤'
)
    comment 'Agentå®šä¹‰è¡¨' charset = utf8mb4;

create index idx_enabled
    on agents (is_enabled);

create index idx_tenant_type
    on agents (tenant_id, agent_type);

create table if not exists audit_log
(
    id            bigint auto_increment comment 'ä¸»é”®'
        primary key,
    tenant_id     varchar(64) default 'tenant_default'  not null comment 'ç§Ÿæˆ·ID',
    user_id       bigint                                null comment 'æ“ä½œç”¨æˆ·ID',
    username      varchar(64)                           null comment 'ç”¨æˆ·å',
    action        varchar(32)                           not null comment 'æ“ä½œç±»åž‹: LOGIN/LOGOUT/CREATE/UPDATE/DELETE/EXPORT/IMPORT',
    resource      varchar(64)                           not null comment 'èµ„æºç±»åž‹: USER/ROLE/ORG/KNOWLEDGE/AGENTç­‰',
    resource_id   varchar(64)                           null comment 'èµ„æºID',
    resource_name varchar(256)                          null comment 'èµ„æºåç§°',
    detail        json                                  null comment 'æ“ä½œè¯¦æƒ…ï¼ˆJSONæ ¼å¼ï¼‰',
    result        varchar(16) default 'SUCCESS'         not null comment 'æ“ä½œç»“æžœ: SUCCESS/FAILURE',
    error_message text                                  null comment 'é”™è¯¯ä¿¡æ¯',
    ip            varchar(64)                           null comment 'è¯·æ±‚IP',
    user_agent    varchar(512)                          null comment 'User-Agent',
    duration      bigint                                null comment 'è¯·æ±‚è€—æ—¶ï¼ˆæ¯«ç§’ï¼‰',
    trace_id      varchar(64)                           null comment 'é“¾è·¯è¿½è¸ªID',
    created_at    datetime    default CURRENT_TIMESTAMP not null comment 'åˆ›å»ºæ—¶é—´'
)
    comment 'å®¡è®¡æ—¥å¿—è¡¨';

create index idx_action
    on audit_log (action);

create index idx_created_at
    on audit_log (created_at);

create index idx_resource
    on audit_log (resource);

create index idx_tenant_id
    on audit_log (tenant_id);

create index idx_trace_id
    on audit_log (trace_id);

create index idx_user_id
    on audit_log (user_id);

create table if not exists conversation_history
(
    id                 bigint auto_increment comment 'ID'
        primary key,
    tenant_id          varchar(64)                         not null comment 'ç§Ÿæˆ·ID',
    user_id            varchar(64)                         not null comment 'ç”¨æˆ·ID',
    session_id         varchar(64)                         not null comment 'ä¼šè¯ID',
    question           text                                not null comment 'ç”¨æˆ·é—®é¢˜',
    answer             text                                null comment 'Agentå›žç­”',
    answer_summary     text                                null comment 'ç­”æ¡ˆæ‘˜è¦ (ç”¨äºŽå‘é‡åŒ–)',
    agent_type         varchar(50)                         null comment 'ä½¿ç”¨çš„Agentç±»åž‹',
    cited_docs         json                                null comment 'å¼•ç”¨çš„æ–‡æ¡£',
    satisfaction_score int                                 null comment 'æ»¡æ„åº¦è¯„åˆ† (1-5)',
    created_at         timestamp default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´'
)
    comment 'å¯¹è¯åŽ†å²è¡¨' charset = utf8mb4;

create index idx_created_at
    on conversation_history (created_at);

create index idx_session
    on conversation_history (session_id);

create index idx_tenant_user
    on conversation_history (tenant_id, user_id);

create table if not exists function_invocations
(
    id                bigint auto_increment comment 'ID'
        primary key,
    tenant_id         varchar(64)                         not null comment 'ç§Ÿæˆ·ID',
    user_id           varchar(64)                         null comment 'ç”¨æˆ·ID',
    session_id        varchar(64)                         null comment 'ä¼šè¯ID',
    function_name     varchar(100)                        not null comment 'Functionåç§°',
    input_params      json                                null comment 'è¾“å…¥å‚æ•°',
    output_result     json                                null comment 'è¾“å‡ºç»“æžœ',
    status            varchar(20)                         not null comment 'çŠ¶æ€ (success/failed)',
    error_message     text                                null comment 'é”™è¯¯ä¿¡æ¯',
    execution_time_ms int                                 null comment 'æ‰§è¡Œæ—¶é—´(æ¯«ç§’)',
    created_at        timestamp default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´'
)
    comment 'Functionè°ƒç”¨æ—¥å¿—è¡¨' charset = utf8mb4;

create index idx_created_at
    on function_invocations (created_at);

create index idx_function
    on function_invocations (function_name);

create index idx_tenant_user
    on function_invocations (tenant_id, user_id);

create table if not exists functions
(
    id                   bigint auto_increment comment 'ID'
        primary key,
    function_name        varchar(100)                         not null comment 'Functionåç§°',
    description          text                                 null comment 'æè¿°',
    parameters_schema    json                                 not null comment 'å‚æ•°Schema (JSON Schema)',
    implementation_class varchar(200)                         not null comment 'å®žçŽ°ç±»å…¨é™å®šå',
    is_enabled           tinyint(1) default 1                 null comment 'æ˜¯å¦å¯ç”¨',
    created_at           timestamp  default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´',
    updated_at           timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'æ›´æ–°æ—¶é—´',
    deleted              tinyint(1) default 0                 null comment 'é€»è¾‘åˆ é™¤',
    constraint function_name
        unique (function_name)
)
    comment 'Functionå®šä¹‰è¡¨' charset = utf8mb4;

create index idx_enabled
    on functions (is_enabled);

create table if not exists knowledge_chunks
(
    id            bigint auto_increment comment 'ID'
        primary key,
    tenant_id     varchar(64)                         not null comment 'ç§Ÿæˆ·ID',
    doc_id        varchar(64)                         not null comment 'æ–‡æ¡£ID',
    chunk_id      varchar(64)                         not null comment 'åˆ†å—ID',
    chunk_index   int                                 not null comment 'åˆ†å—åºå·',
    chunk_content text                                not null comment 'åˆ†å—å†…å®¹',
    metadata      json                                null comment 'å…ƒæ•°æ®',
    created_at    timestamp default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´',
    constraint chunk_id
        unique (chunk_id)
)
    comment 'æ–‡æ¡£åˆ†å—è¡¨' charset = utf8mb4;

create index idx_doc_id
    on knowledge_chunks (doc_id);

create index idx_tenant
    on knowledge_chunks (tenant_id);

create table if not exists knowledge_docs
(
    id             bigint auto_increment comment 'ID'
        primary key,
    tenant_id      varchar(64)                          not null comment 'ç§Ÿæˆ·ID',
    space_id       varchar(64)                          null comment 'çŸ¥è¯†ç©ºé—´ID',
    doc_id         varchar(64)                          not null comment 'æ–‡æ¡£ID',
    doc_title      varchar(200)                         not null comment 'æ–‡æ¡£æ ‡é¢˜',
    doc_type       varchar(50)                          null comment 'æ–‡æ¡£ç±»åž‹ (policy/procedure/faq)',
    category       varchar(100)                         null comment 'åˆ†ç±»',
    content        longtext                             not null comment 'æ–‡æ¡£å†…å®¹',
    metadata       json                                 null comment 'å…ƒæ•°æ®',
    allowed_roles  json                                 null comment 'å…è®¸è®¿é—®çš„è§’è‰²',
    vector_indexed tinyint(1) default 0                 null comment 'æ˜¯å¦å·²å‘é‡åŒ–',
    created_at     timestamp  default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´',
    updated_at     timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'æ›´æ–°æ—¶é—´',
    deleted        tinyint(1) default 0                 null comment 'é€»è¾‘åˆ é™¤',
    constraint doc_id
        unique (doc_id)
)
    comment 'çŸ¥è¯†åº“æ–‡æ¡£è¡¨' charset = utf8mb4;

create index idx_category
    on knowledge_docs (category);

create index idx_tenant_space
    on knowledge_docs (tenant_id, space_id);

create index idx_vector_indexed
    on knowledge_docs (vector_indexed);

create table if not exists org_unit
(
    id         bigint auto_increment comment 'ä¸»é”®'
        primary key,
    tenant_id  varchar(64) default 'tenant_default'  not null comment 'ç§Ÿæˆ·ID',
    parent_id  bigint      default 0                 not null comment 'çˆ¶çº§IDï¼ˆ0è¡¨ç¤ºé¡¶çº§ï¼‰',
    name       varchar(128)                          not null comment 'ç»„ç»‡åç§°',
    code       varchar(64)                           not null comment 'ç»„ç»‡ç¼–ç ï¼ˆå”¯ä¸€æ ‡è¯†ï¼‰',
    type       varchar(32) default 'dept'            not null comment 'ç±»åž‹: company=å…¬å¸, dept=éƒ¨é—¨, team=å›¢é˜Ÿ',
    path       varchar(512)                          null comment 'ç»„ç»‡å…¨è·¯å¾„ï¼ˆå¦‚: /1/2/3/ï¼‰',
    level      int         default 1                 not null comment 'å±‚çº§æ·±åº¦ï¼ˆä»Ž1å¼€å§‹ï¼‰',
    leader_id  bigint                                null comment 'è´Ÿè´£äººç”¨æˆ·ID',
    sort_order int         default 0                 not null comment 'æŽ’åº',
    status     tinyint     default 1                 not null comment 'çŠ¶æ€: 0=ç¦ç”¨, 1=å¯ç”¨',
    created_at datetime    default CURRENT_TIMESTAMP not null comment 'åˆ›å»ºæ—¶é—´',
    updated_at datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'æ›´æ–°æ—¶é—´',
    deleted    tinyint     default 0                 not null comment 'é€»è¾‘åˆ é™¤: 0=æ­£å¸¸, 1=å·²åˆ é™¤',
    constraint uk_tenant_code
        unique (tenant_id, code, deleted)
)
    comment 'ç»„ç»‡å•å…ƒè¡¨';

create index idx_parent_id
    on org_unit (parent_id);

create index idx_path
    on org_unit (path(255));

create index idx_tenant_id
    on org_unit (tenant_id);

create table if not exists sys_role
(
    id          bigint auto_increment comment 'ä¸»é”®'
        primary key,
    tenant_id   varchar(64) default 'tenant_default'  not null comment 'ç§Ÿæˆ·ID',
    name        varchar(64)                           not null comment 'è§’è‰²åç§°',
    code        varchar(64)                           not null comment 'è§’è‰²ç¼–ç ï¼ˆå”¯ä¸€æ ‡è¯†ï¼‰',
    description varchar(256)                          null comment 'è§’è‰²æè¿°',
    permissions json                                  null comment 'æƒé™åˆ—è¡¨ï¼ˆJSONæ•°ç»„ï¼‰',
    data_scope  tinyint     default 1                 not null comment 'æ•°æ®èŒƒå›´: 1=å…¨éƒ¨, 2=æœ¬éƒ¨é—¨åŠå­éƒ¨é—¨, 3=ä»…æœ¬éƒ¨é—¨, 4=ä»…æœ¬äºº',
    status      tinyint     default 1                 not null comment 'çŠ¶æ€: 0=ç¦ç”¨, 1=å¯ç”¨',
    sort_order  int         default 0                 not null comment 'æŽ’åº',
    created_at  datetime    default CURRENT_TIMESTAMP not null comment 'åˆ›å»ºæ—¶é—´',
    updated_at  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'æ›´æ–°æ—¶é—´',
    deleted     tinyint     default 0                 not null comment 'é€»è¾‘åˆ é™¤: 0=æ­£å¸¸, 1=å·²åˆ é™¤',
    constraint uk_tenant_code
        unique (tenant_id, code, deleted)
)
    comment 'ç³»ç»Ÿè§’è‰²è¡¨';

create index idx_tenant_id
    on sys_role (tenant_id);

create table if not exists sys_user
(
    id              bigint auto_increment comment 'ä¸»é”®'
        primary key,
    tenant_id       varchar(64) default 'tenant_default'  not null comment 'ç§Ÿæˆ·ID',
    username        varchar(64)                           not null comment 'ç”¨æˆ·åï¼ˆç™»å½•è´¦å·ï¼‰',
    password        varchar(128)                          not null comment 'å¯†ç ï¼ˆBCryptåŠ å¯†ï¼‰',
    real_name       varchar(64)                           null comment 'çœŸå®žå§“å',
    email           varchar(128)                          null comment 'é‚®ç®±',
    phone           varchar(20)                           null comment 'æ‰‹æœºå·',
    avatar          varchar(512)                          null comment 'å¤´åƒURL',
    status          tinyint     default 1                 not null comment 'çŠ¶æ€: 0=ç¦ç”¨, 1=å¯ç”¨',
    role_id         bigint                                null comment 'è§’è‰²ID',
    org_unit_id     bigint                                null comment 'ç»„ç»‡å•å…ƒID',
    plan_type       varchar(32) default 'free'            not null comment 'å¥—é¤ç±»åž‹: free/pro/enterprise',
    last_login_time datetime                              null comment 'æœ€åŽç™»å½•æ—¶é—´',
    last_login_ip   varchar(64)                           null comment 'æœ€åŽç™»å½•IP',
    created_at      datetime    default CURRENT_TIMESTAMP not null comment 'åˆ›å»ºæ—¶é—´',
    updated_at      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'æ›´æ–°æ—¶é—´',
    deleted         tinyint     default 0                 not null comment 'é€»è¾‘åˆ é™¤: 0=æ­£å¸¸, 1=å·²åˆ é™¤',
    constraint uk_tenant_username
        unique (tenant_id, username, deleted)
)
    comment 'ç³»ç»Ÿç”¨æˆ·è¡¨';

create index idx_email
    on sys_user (email);

create index idx_org_unit_id
    on sys_user (org_unit_id);

create index idx_phone
    on sys_user (phone);

create index idx_role_id
    on sys_user (role_id);

create index idx_tenant_id
    on sys_user (tenant_id);

create table if not exists tenants
(
    id           bigint auto_increment comment 'ID'
        primary key,
    tenant_id    varchar(64)                          not null comment 'ç§Ÿæˆ·ID',
    tenant_name  varchar(100)                         not null comment 'ç§Ÿæˆ·åç§°',
    plan_type    varchar(50)                          not null comment 'å¥—é¤ç±»åž‹ (free/pro/enterprise)',
    token_budget int        default 0                 null comment 'Tokené¢„ç®— (æ¯æœˆ)',
    token_used   int        default 0                 null comment 'å·²ä½¿ç”¨Token',
    is_active    tinyint(1) default 1                 null comment 'æ˜¯å¦æ¿€æ´»',
    created_at   timestamp  default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´',
    updated_at   timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'æ›´æ–°æ—¶é—´',
    deleted      tinyint(1) default 0                 null comment 'é€»è¾‘åˆ é™¤',
    constraint tenant_id
        unique (tenant_id)
)
    comment 'ç§Ÿæˆ·é…ç½®è¡¨' charset = utf8mb4;

create index idx_active
    on tenants (is_active);

create table if not exists token_usage
(
    id                bigint auto_increment comment 'ID'
        primary key,
    tenant_id         varchar(64)                         not null comment 'ç§Ÿæˆ·ID',
    user_id           varchar(64)                         null comment 'ç”¨æˆ·ID',
    session_id        varchar(64)                         null comment 'ä¼šè¯ID',
    model_name        varchar(50)                         null comment 'æ¨¡åž‹åç§°',
    prompt_tokens     int                                 null comment 'æç¤ºTokenæ•°',
    completion_tokens int                                 null comment 'å®ŒæˆTokenæ•°',
    total_tokens      int                                 null comment 'æ€»Tokenæ•°',
    created_at        timestamp default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´'
)
    comment 'Tokenä½¿ç”¨è®°å½•è¡¨' charset = utf8mb4;

create index idx_session
    on token_usage (session_id);

create index idx_tenant_date
    on token_usage (tenant_id, created_at);

create table if not exists user_profiles
(
    id                      bigint auto_increment comment 'ID'
        primary key,
    tenant_id               varchar(64)                          not null comment 'ç§Ÿæˆ·ID',
    user_id                 varchar(64)                          not null comment 'ç”¨æˆ·ID',
    department              varchar(100)                         null comment 'éƒ¨é—¨',
    position                varchar(100)                         null comment 'èŒä½',
    frequently_asked_topics json                                 null comment 'å¸¸é—®è¯é¢˜ (JSONæ•°ç»„)',
    preferences             json                                 null comment 'ç”¨æˆ·åå¥½',
    total_questions         int        default 0                 null comment 'æ€»æé—®æ•°',
    avg_satisfaction        decimal(3, 2)                        null comment 'å¹³å‡æ»¡æ„åº¦',
    last_interaction_at     timestamp                            null comment 'æœ€åŽäº¤äº’æ—¶é—´',
    created_at              timestamp  default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´',
    updated_at              timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'æ›´æ–°æ—¶é—´',
    deleted                 tinyint(1) default 0                 null comment 'é€»è¾‘åˆ é™¤',
    constraint uk_tenant_user
        unique (tenant_id, user_id)
)
    comment 'ç”¨æˆ·ç”»åƒè¡¨' charset = utf8mb4;

create index idx_department
    on user_profiles (department);

create table if not exists workflow_executions
(
    id                bigint auto_increment comment 'ID'
        primary key,
    tenant_id         varchar(64)                         not null comment 'ç§Ÿæˆ·ID',
    workflow_id       bigint                              not null comment 'å·¥ä½œæµID',
    execution_id      varchar(64)                         not null comment 'æ‰§è¡ŒID',
    status            varchar(20)                         not null comment 'çŠ¶æ€ (running/completed/failed)',
    current_step      varchar(100)                        null comment 'å½“å‰æ­¥éª¤',
    execution_context json                                null comment 'æ‰§è¡Œä¸Šä¸‹æ–‡',
    start_time        timestamp default CURRENT_TIMESTAMP null comment 'å¼€å§‹æ—¶é—´',
    end_time          timestamp                           null comment 'ç»“æŸæ—¶é—´',
    error_message     text                                null comment 'é”™è¯¯ä¿¡æ¯',
    created_at        timestamp default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´',
    constraint execution_id
        unique (execution_id)
)
    comment 'å·¥ä½œæµæ‰§è¡Œè®°å½•è¡¨' charset = utf8mb4;

create index idx_execution_id
    on workflow_executions (execution_id);

create index idx_status
    on workflow_executions (status);

create index idx_tenant_workflow
    on workflow_executions (tenant_id, workflow_id);

create table if not exists workflows
(
    id                 bigint auto_increment comment 'ID'
        primary key,
    tenant_id          varchar(64)                          not null comment 'ç§Ÿæˆ·ID',
    workflow_name      varchar(100)                         not null comment 'å·¥ä½œæµåç§°',
    description        text                                 null comment 'æè¿°',
    dag_definition     json                                 not null comment 'DAGå®šä¹‰ (èŠ‚ç‚¹å’Œè¾¹)',
    trigger_conditions json                                 null comment 'è§¦å‘æ¡ä»¶',
    is_enabled         tinyint(1) default 1                 null comment 'æ˜¯å¦å¯ç”¨',
    created_at         timestamp  default CURRENT_TIMESTAMP null comment 'åˆ›å»ºæ—¶é—´',
    updated_at         timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment 'æ›´æ–°æ—¶é—´',
    deleted            tinyint(1) default 0                 null comment 'é€»è¾‘åˆ é™¤'
)
    comment 'å·¥ä½œæµå®šä¹‰è¡¨' charset = utf8mb4;

create index idx_enabled
    on workflows (is_enabled);

create index idx_tenant
    on workflows (tenant_id);

