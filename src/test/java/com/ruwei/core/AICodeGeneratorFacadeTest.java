package com.ruwei.core;

import com.ruwei.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AICodeGeneratorFacadeTest {
    @Resource
    private AICodeGeneratorFacade aiCodeGeneratorFacade;

/*    @Test
    void generateAndSaveCode()  {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("登录页面，不能超过30行", CodeGenTypeEnum.MULTI_FILE,1L);
        Assertions.assertNotNull(file);
    }*/

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream("登录页面，不能超过30行", CodeGenTypeEnum.MULTI_FILE,2L);
        //阻塞所有数据等待
        List<String> result = stringFlux.collectList().block();
        //验证结果
        Assertions.assertNotNull(result);
        //拼接字符串，得到完整内容
        String join = String.join("\n", result);
        Assertions.assertNotNull(join);
    }
}