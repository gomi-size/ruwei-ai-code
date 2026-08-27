package com.ruwei.ruweicodeapp;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.ruwei.ruweicodeapp.mapper")
@EnableCaching
@ComponentScan(basePackages = "com.ruwei")
public class RuweiAiCodeAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuweiAiCodeAppApplication.class, args);
    }
}
