package com.ruwei.ruweicodeapp.ai;

import com.ruwei.ai.AiCodeGenTypeRoutingService;
import com.ruwei.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成类型路由服务工厂
 *
 * @author wudi
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {


    /**
     * 创建AI代码生成类型路由服务实例
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        ChatModel routingChatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(routingChatModel)
                .build();
    }

    /**
     * 默认提供一个Bean
     * @return
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService(){
        return createAiCodeGenTypeRoutingService();
    }
}
