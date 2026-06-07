package com.ruwei.ai;

import com.ruwei.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型智能路由服务
 * 使用结构化输出直接返回枚举类型
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户需求智能选择代码生成类型
     * @param userPrompt
     * @return
     */
    @SystemMessage(fromResource = "prompt/dd.txt")
    CodeGenTypeEnum routeCodeType(String userPrompt);

}
