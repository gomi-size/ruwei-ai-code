package com.ruwei.ruweicodeapp.core.parser;

import com.ruwei.exception.BusinessException;
import com.ruwei.exception.ErrorCode;
import com.ruwei.model.enums.CodeGenTypeEnum;

/**
 * 代码解析器
 */
public class CodeParserExecutor {

    private static final  HtmlCodeParser  htmlCodeParser = new HtmlCodeParser();
    private static final  MultiFileCodeParser  multiFileCodeParser = new MultiFileCodeParser();


    /**
     * 代码解析
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代生成的代码类型
     * @return
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum){
        return switch (codeGenTypeEnum){
            case HTML ->  htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"不支持改类型文件");
        };
    }
}
