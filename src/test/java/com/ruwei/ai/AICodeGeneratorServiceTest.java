package com.ruwei.ai;

import com.ruwei.ai.model.HtmlCodeResult;
import com.ruwei.core.AICodeGeneratorFacade;
import com.ruwei.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class AICodeGeneratorServiceTest {
    @Resource
    private AICodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateHtmlCode() {
        //File file = aiCodeGeneratorFacade.generateAndSaveCode("帮我做一个工作留言板", CodeGenTypeEnum.HTML);
       // Assertions.assertNotNull(file);
    }

    @Test
    void generateMultiFileCode() {
    }
}