package com.ruwei.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ruwei.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.memory.ChatMemoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
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
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI服务实例缓存
     */
    private final Cache<Long, AICodeGeneratorService> cache = Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(Duration.ofMillis(30))
            .expireAfterAccess(Duration.ofMillis(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI服务实例被移除，appId:{},原因：{}",key, cause);
            }).build();

    /**
     * 根据appid获取服务
     * @param appId
     * @return
     */
    public AICodeGeneratorService getAICodeGeneratorService(Long appId) {
        //根据appId构建独立的对话记忆
        return cache.get(appId, this::createAICodeGeneratorService);
    }

    /**
     * 创建新的ai服务，初始化AI新服务
     * @param appId
     * @return
     */
    public AICodeGeneratorService createAICodeGeneratorService(Long appId) {
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
        return AiServices.builder(AICodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                // 根据 id 构建独立的对话记忆
                .chatMemory(chatMemory)
                .build();
    }
}
