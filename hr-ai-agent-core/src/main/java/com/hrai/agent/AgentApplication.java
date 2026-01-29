package com.hrai.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Agent核心服务启动类
 *
 * @author HR AI Team
 */
@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.hrai.agent", "com.hrai.common"})
@MapperScan("com.hrai.agent.**.mapper")
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
        System.out.println("""

                ╔═══════════════════════════════════════════════════╗
                ║                                                   ║
                ║    HR AI Agent 核心服务启动成功!                    ║
                ║                                                   ║
                ║    多Agent协作系统已就绪                            ║
                ║    访问 http://localhost:8080/actuator/health     ║
                ║                                                   ║
                ╚═══════════════════════════════════════════════════╝
                """);
    }
}
