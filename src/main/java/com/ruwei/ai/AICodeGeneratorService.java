package com.ruwei.ai;

import com.ruwei.ai.model.HtmlCodeResult;
import com.ruwei.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

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


}
