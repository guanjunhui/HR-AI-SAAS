-- ============================================
-- HR AI SaaS - 初始化数据
-- ============================================

USE hr_ai_saas;

-- ============================================
-- 1. 初始化租户数据
-- ============================================

INSERT INTO tenants (tenant_id, tenant_name, plan_type, token_budget, is_active)
VALUES
    ('tenant_default', '默认租户', 'pro', 1000000, 1),
    ('tenant_demo', '演示租户', 'free', 100000, 1);

-- ============================================
-- 2. 初始化 Agent 定义
-- ============================================

INSERT INTO agents (tenant_id, agent_type, agent_name, description, system_prompt, model_name, temperature, max_tokens, tools_config, is_enabled)
VALUES
    ('tenant_default', 'hr_policy', 'HR政策专家', '专门回答HR政策、流程、规章制度相关问题',
     '你是一位专业的HR政策专家,负责解答员工关于公司HR政策、流程和规章制度的问题。\n\n你的职责:\n1. 准确解释公司的HR政策和流程\n2. 引用具体的政策文档和条款\n3. 如果政策不明确,诚实告知用户并建议联系HR部门\n4. 保持专业、友好的语气\n\n回答规范:\n- 必须基于检索到的知识库文档回答\n- 引用来源时使用 [文档名称] 格式\n- 如果找不到相关信息,不要编造,建议用户联系HR\n- 回答要简洁明了,分点说明',
     'qwen-plus', 0.7, 2000, '["search_knowledge", "get_policy_doc"]', 1),

    ('tenant_default', 'recruiting', '招聘助手', '处理招聘流程、候选人查询等问题',
     '你是招聘流程助手,帮助HR和候选人处理招聘相关问题。\n\n你的职责:\n1. 解释招聘流程和要求\n2. 查询候选人状态和面试安排\n3. 回答招聘政策问题\n\n注意事项:\n- 保护候选人隐私\n- 只向授权用户提供候选人信息\n- 招聘决策必须由人工做出',
     'qwen-plus', 0.7, 2000, '["search_candidates", "get_interview_schedule", "search_knowledge"]', 1),

    ('tenant_default', 'ticket_router', '工单路由助手', '智能路由工单到合适的处理团队',
     '你是工单路由助手,负责分析员工问题并路由到合适的处理团队。\n\n路由规则:\n1. HR政策类 → HR团队\n2. 薪资福利类 → 薪酬团队\n3. IT技术类 → IT支持\n4. 其他 → 综合服务团队\n\n你需要:\n- 分析问题类型\n- 评估紧急程度 (低/中/高)\n- 提取关键信息',
     'qwen-turbo', 0.5, 1000, '["create_ticket", "route_ticket"]', 1);

-- ============================================
-- 3. 初始化 Function 定义
-- ============================================

INSERT INTO functions (function_name, description, parameters_schema, implementation_class, is_enabled)
VALUES
    ('search_knowledge', '在知识库中搜索相关文档和信息',
     '{"type":"object","properties":{"query":{"type":"string","description":"搜索查询词"},"top_k":{"type":"integer","description":"返回Top K结果","default":5},"category":{"type":"string","description":"文档分类(可选)"}},"required":["query"]}',
     'com.hrai.agent.tools.impl.SearchKnowledgeFunction', 1),

    ('get_policy_doc', '获取完整的政策文档内容',
     '{"type":"object","properties":{"doc_id":{"type":"string","description":"文档ID"}},"required":["doc_id"]}',
     'com.hrai.agent.tools.impl.GetPolicyDocFunction', 1),

    ('search_candidates', '搜索招聘候选人信息',
     '{"type":"object","properties":{"query":{"type":"string","description":"搜索关键词(姓名、职位等)"},"status":{"type":"string","description":"候选人状态(面试中/待定/已录用/已拒绝)"}},"required":["query"]}',
     'com.hrai.agent.tools.impl.SearchCandidatesFunction', 1),

    ('get_interview_schedule', '获取候选人的面试安排',
     '{"type":"object","properties":{"candidate_id":{"type":"string","description":"候选人ID"}},"required":["candidate_id"]}',
     'com.hrai.agent.tools.impl.GetInterviewScheduleFunction', 1),

    ('create_ticket', '创建新的工单',
     '{"type":"object","properties":{"title":{"type":"string","description":"工单标题"},"description":{"type":"string","description":"工单描述"},"category":{"type":"string","description":"工单分类"},"priority":{"type":"string","description":"优先级(低/中/高)"}},"required":["title","description","category"]}',
     'com.hrai.agent.tools.impl.CreateTicketFunction', 1),

    ('route_ticket', '将工单路由到合适的团队',
     '{"type":"object","properties":{"ticket_id":{"type":"string","description":"工单ID"},"team":{"type":"string","description":"目标团队"},"reason":{"type":"string","description":"路由原因"}},"required":["ticket_id","team"]}',
     'com.hrai.agent.tools.impl.RouteTicketFunction', 1);

-- ============================================
-- 4. 初始化测试知识库数据
-- ============================================

INSERT INTO knowledge_docs (tenant_id, space_id, doc_id, doc_title, doc_type, category, content, allowed_roles, vector_indexed)
VALUES
    ('tenant_default', 'default', 'doc_leave_policy', '请假管理制度', 'policy', 'HR政策',
     '# 请假管理制度\n\n## 1. 适用范围\n本制度适用于公司全体员工。\n\n## 2. 请假类型\n\n### 2.1 年假\n- 工作满1年: 5天\n- 工作满3年: 10天\n- 工作满5年: 15天\n\n### 2.2 病假\n- 每年最多15天\n- 需提供医院证明\n- 病假期间工资按80%发放\n\n### 2.3 事假\n- 每年最多10天\n- 事假期间无工资\n- 需提前3天申请\n\n## 3. 请假流程\n1. 在HR系统提交请假申请\n2. 直属主管审批\n3. HR部门备案\n4. 超过3天需要部门总监审批\n\n## 4. 注意事项\n- 紧急情况可先电话告知,事后补办手续\n- 虚假请假将受到纪律处分',
     '["all"]', 0),

    ('tenant_default', 'default', 'doc_onboarding', '新员工入职流程', 'procedure', 'HR流程',
     '# 新员工入职流程\n\n## 入职前准备(Offer发出后)\n1. HR发送入职通知邮件\n2. 新员工准备入职材料:\n   - 身份证复印件\n   - 学历学位证书复印件\n   - 离职证明(如有)\n   - 银行卡信息\n\n## 入职当天\n1. 9:00 到HR部门报到\n2. 签署劳动合同\n3. 填写员工信息表\n4. 领取工牌和办公用品\n5. IT部门开通系统账号\n6. 部门主管带领参观办公区\n\n## 入职第一周\n1. 参加新员工培训\n2. 了解公司规章制度\n3. 熟悉团队成员\n4. 学习业务流程\n\n## 试用期\n- 试用期为3个月\n- 每月一次试用期评估\n- 转正需通过部门总监审批',
     '["all"]', 0),

    ('tenant_default', 'default', 'doc_salary_faq', '薪资福利常见问题', 'faq', '薪酬福利',
     '# 薪资福利常见问题\n\n## Q1: 工资何时发放?\nA: 每月15日发放上月工资。如遇节假日则提前发放。\n\n## Q2: 社保公积金缴纳比例?\nA: \n- 养老保险: 公司16%,个人8%\n- 医疗保险: 公司10%,个人2%\n- 失业保险: 公司0.5%,个人0.5%\n- 住房公积金: 公司12%,个人12%\n\n## Q3: 年终奖如何发放?\nA: 根据公司业绩和个人绩效,在次年2月发放。\n\n## Q4: 加班费如何计算?\nA: \n- 工作日加班: 1.5倍工资\n- 周末加班: 2倍工资\n- 法定节假日: 3倍工资\n\n## Q5: 如何申请调薪?\nA: 每年6月和12月是调薪周期,需要:\n1. 绩效评估达标\n2. 直属主管推荐\n3. 部门总监审批',
     '["all"]', 0);

-- ============================================
-- 5. 初始化用户画像测试数据
-- ============================================

INSERT INTO user_profiles (tenant_id, user_id, department, position, frequently_asked_topics, total_questions, avg_satisfaction)
VALUES
    ('tenant_default', 'user_001', '技术部', '软件工程师', '["请假流程", "加班费", "社保"]', 15, 4.5),
    ('tenant_default', 'user_002', 'HR部', 'HR专员', '["招聘流程", "入职手续", "劳动合同"]', 28, 4.8);
