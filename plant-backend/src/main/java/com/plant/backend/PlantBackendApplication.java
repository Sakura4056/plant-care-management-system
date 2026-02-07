package com.plant.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import org.springframework.scheduling.annotation.EnableScheduling;

// 排除默认的DataSourceAutoConfiguration，因为我们使用自定义的多数据源配置
@EnableScheduling
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class PlantBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantBackendApplication.class, args);
    }

}
