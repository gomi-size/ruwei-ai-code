package com.ruwei.core;

import com.ruwei.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AICodeGeneratorFacadeTest {
    @Resource
    private AICodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode()  {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("登录页面，不能超过30行", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }
}