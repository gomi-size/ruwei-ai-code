package com.ruwei.ai;

import com.ruwei.ai.model.HtmlCodeResult;
import com.ruwei.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AICodeGeneratorService {

    /**
     * 生成HTML代码
     * @param userMessage 用户提示词
     * @return AI输出结果
     */
    @SystemMessage(fromResource = "prompt/aa.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);


    /**
     * 生成多文件代码
     * @param userMessage 用户提示词
     * @return AI输出结果
     */
    @SystemMessage(fromResource = "prompt/bb.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成HTML代码(流式输出)
     * @param userMessage 用户提示词
     * @return AI输出结果
     */

    @SystemMessage(fromResource = "prompt/aa.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);


    /**
     * 生成多文件代码（流式输出）
     * @param userMessage 用户提示词
     * @return AI输出结果
     */
    @SystemMessage(fromResource = "prompt/bb.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
    /**
     * 生成Vue项目代码生成流式
     * @param userMessage 用户提示词
     * @MemoryId Long appId
     * @return AI输出结果
     */
    @SystemMessage(fromResource = "prompt/cc.txt")
    Flux<String> generateVueProjectCodeStream(@MemoryId Long appId, @UserMessage String userMessage);


}
