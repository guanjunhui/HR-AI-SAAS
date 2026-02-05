package com.hrai.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * HR 业务服务启动类
 *
 * 功能：
 * - 员工管理 (Employee)
 * - 岗位管理 (Position)
 * - 任职事件 (EmploymentEvent)
 * - 编制管理 (Headcount)
 * - 考勤班次 (AttendanceShift)
 * - 假期类型 (LeaveType)
 * - 薪酬项目 (SalaryItem)
 */
@SpringBootApplication(scanBasePackages = {
    "com.hrai.business",
    "com.hrai.common"
})
@EnableDiscoveryClient
@MapperScan("com.hrai.business.mapper")
public class BusinessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessServiceApplication.class, args);
    }
}
