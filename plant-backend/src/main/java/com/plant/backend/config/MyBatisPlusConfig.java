package com.plant.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 由于我们有多种数据库类型，且 MySQL 是主要的，我们通常使用 MYSQL 方言。
        // 分页逻辑大多是 SQL 标准，但 limit/offset 语法有所不同。
        // MP 通常自动从数据源检测 DB 类型，但动态/多数据源的有效配置比较棘手。
        // 我们添加拦截器而不指定特定的 DbType 让 MP 自动检测，
        // 或者如果需要，我们添加两个拦截器（但 MP 文档说如果全局配置，一个通常就够了）。
        // 让我们配置为 MYSQL 作为主要默认值。
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 注意: SQLite 分页语法 (LIMIT OFFSET) 与 MySQL 兼容
        return interceptor;
    }
}
