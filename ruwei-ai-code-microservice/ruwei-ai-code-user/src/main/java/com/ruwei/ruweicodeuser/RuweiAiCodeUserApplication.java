package com.ruwei.ruweicodeuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.ruwei.ruweicodeuser.mapper")
@ComponentScan("com.ruwei")
public class RuweiAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuweiAiCodeUserApplication.class, args);
    }
}
