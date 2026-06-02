package com.ruwei.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ruwei.ai.tools.FileWriteTool;
import com.ruwei.exception.BusinessException;
import com.ruwei.exception.ErrorCode;
import com.ruwei.model.enums.CodeGenTypeEnum;
import com.ruwei.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * ai服务创建工厂
 */
@Slf4j
@Configuration
public class AICodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel openAiStreamingChatModel;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI服务实例缓存
     */
    private final Cache<String, AICodeGeneratorService> cache = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(Duration.ofMillis(30))
            .expireAfterAccess(Duration.ofMillis(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI服务实例被移除，缓存键:{},原因：{}",key, cause);
            }).build();

/*    *//**
     * 根据appid获取服务(兼容老方法)
     * @param appId
     * @return
     *//*
    public AICodeGeneratorService getAICodeGeneratorService(Long appId) {
        //根据appId构建独立的对话记忆
        return getAICodeGeneratorService(appId, CodeGenTypeEnum.MULTI_FILE);
    }*/
    /**
     * 根据appid获取服务
     * @param appId
     * @return
     */
    public AICodeGeneratorService getAICodeGeneratorService(Long appId,CodeGenTypeEnum codeGenTypeEnum) {
        String buildCacheKey = buildCacheKey(appId, codeGenTypeEnum);
        //根据appId构建独立的对话记忆
        return cache.get(buildCacheKey, key->createAICodeGeneratorService(appId,codeGenTypeEnum));
    }

    /**
     * 创建新的AI服务，(这个是创建更为复杂的ai服务)
     * @param appId
     * @param codeGenTypeEnum
     * @return
     */
    public AICodeGeneratorService createAICodeGeneratorService(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        log.info("为{}，创建新的AI服务",appId);
        //根据appId构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        //从数据库中加载对话记忆
        chatHistoryService.loadChatHistoryMemory(appId, chatMemory,20);
        return switch (codeGenTypeEnum){
            //Vue项目生成就使用工具调用和项目推理
            case VUE_PROJECT -> AiServices.builder(AICodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(reasoningStreamingChatModel)
                    // 根据 id 构建独立的对话记忆
                    .chatMemoryProvider(memory->chatMemory)
                    .tools(new FileWriteTool())
                    //处理工具幻觉问题
                    .hallucinatedToolNameStrategy(toolExecutionRequest ->
                            ToolExecutionResultMessage.from(toolExecutionRequest,
                                    "Error :there is no tool called"+toolExecutionRequest.name()))
                    .build();
            //普通的就是HTML和多文件生成，使用流式对话模型
            case HTML ,MULTI_FILE->AiServices.builder(AICodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    // 根据 id 构建独立的对话记忆
                    .chatMemory(chatMemory)
                    .build();
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持的代码生成类型");
        };

    }
    /**
     * 构建缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }
}
