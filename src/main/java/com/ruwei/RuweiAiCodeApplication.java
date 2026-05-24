package com.ruwei;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ruwei.mapper")
public class RuweiAiCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuweiAiCodeApplication.class, args);
    }

}
