package com.ruwei.ruweicodeapp.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ruwei.model.dto.chathistory.ChatHistoryQueryRequest;
import com.ruwei.model.entity.ChatHistory;
import com.ruwei.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/gomi-size">入围</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加聊天消息
     * @param appid 应用id
     * @param message 消息内容
     * @param messageType 消息类型
     * @param userId 用户id
     * @return
     */
    boolean addChatMessage(Long appid,String message,String messageType, Long userId);

    /**
     * 游标分页查询
     * @param appId 应用id
     * @param pageSize 每页大小
     * @param lastCreateTime 最后创建时间
     * @param loginUser 登录用户
     * @return
     */

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 加载对话记忆
     * 从系统中加载指定应用ID的对话历史记录，并将其存储到消息窗口记忆对象中
     * @param appId 应用的唯一标识符，用于区分不同应用的对话记忆
     * @param chatMemory 记忆模型
     * @param maxCount 最大的对话记录
     * @return
     */
    int loadChatHistoryMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 构造查询条件
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    boolean deleteByAppId(Long appId);
}
